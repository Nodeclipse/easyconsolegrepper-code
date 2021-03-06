package de.jepfa.easyconsolegrepper;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.TextConsole;

import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.model.ECGModel;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * The Grep Console.This console filters only lines from the observed source
 * {@link TextConsole} whether the given search string was found or the regexp has matched.
 * <p>
 * Parts are copied from http://code.google.com/a/eclipselabs.org/p/console-grep
 *
 * @author Jens Pfahl
 */
public class GrepConsole extends IOConsole implements IDocumentListener {

	private boolean endReached = false;
    private enum RangeMatchingState {IN_RANGE, END_REACHED, NOT_IN_RANGE, END_MATCH};

    private static String LINE_NUMBER_SEPARATOR = ": "; //$NON-NLS-1$

    private static final int SOURCE_CONSOLE_NAME_LENGTH = 40;
    private static final int SEARCH_STRING_LENGTH = 30;


    /**
     * Key = line offset
     * Value = Styles of this line
     */
    private Map<Integer, StyleRange[]> _styleRangeCache = new ConcurrentHashMap<Integer, StyleRange[]>();

    private IOConsoleOutputStream outStream = newOutputStream();
    private int grepDocumentLenth = 0;
    private IDocument watchedDoc;
    private String lineSeperator = "\n"; //$NON-NLS-1$
    private Pattern grepPattern;
    private Pattern grepPatternEnd;
    private RangeMatchingState rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
    private String lineBuff = ""; //$NON-NLS-1$
    private ECGModel ecgModel;
    private int subsequentLineCounter = getSubsequentLines();
    private boolean oldMatching = false;
    private int prevLineNumber;
    private long currentTime = 0;

    public GrepConsole(ECGModel ecgModel) {
        super(Activator.GREP_CONSOLE_NAME, Activator.getImageDescriptor(Activator.IMAGE_GREP_CONSOLE_16));
        // don't limit outputs
        setWaterMarks(-1, -1);

        getInputStream().setColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));

        getDocument().addDocumentListener(new IDocumentListener() {
            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {}
            @Override
            public void documentChanged(DocumentEvent event) {
                IDocument document = event.getDocument();
                // grepDocumentLenth don't consider the inputStream of GrepConsole.
                // We adjust them here.
                if (document.get().length() > grepDocumentLenth || document.get().length() == 0) {
                    grepDocumentLenth = document.get().length();
                }
            }
        });

        updateModel(ecgModel);
    }



    @Override
    public void clearConsole() {
        super.clearConsole();
        _styleRangeCache.clear();
        grepDocumentLenth = 0;
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        lineBuff += event.getText();
        String lines[] = lineBuff.split(lineSeperator);
        if (lineBuff.endsWith(lineSeperator)) {
            lineBuff = ""; //$NON-NLS-1$
        } else {
            lineBuff = lines[lines.length - 1];
        }

        int lineNumber = event.getDocument().getNumberOfLines() - lines.length + 1;
        for (String line : lines) {

        	// when a match occured or all shall be shown everytime...
            if (isMatching(line) || !ecgModel.isFilterEnabled()) {
                int consoleWasSilentTimeRange = getConsoleWasSilentTimeRange();
                if (currentTime != 0 &&
                        consoleWasSilentTimeRange != 0 &&
                        System.currentTimeMillis() - currentTime > consoleWasSilentTimeRange * 1000) {
                    writeToConsole(MessageFormat.format(
                            Messages.GrepConsole_SourceConsoleWasSilent,
                            Activator.GREP_CONSOLE_OUTPUT_PREFIX, consoleWasSilentTimeRange));
                }
                writeToConsole(lineNumber, line);
                currentTime = System.currentTimeMillis();
            }

            lineNumber++;
        }
    }

    public StyleRange[] getCachedStyleRange(int lineOffset) {
        return _styleRangeCache.get(lineOffset);
    }

    public ECGModel getModel() {
        return ecgModel;
    }

    public void refresh() {
        updateModel(ecgModel);
    }

    public void setSource(TextConsole source) {
        if (watchedDoc != null) {
            watchedDoc.removeDocumentListener(this);
        }

        // Start watching source console
        watchedDoc = source.getDocument();
        watchedDoc.addDocumentListener(this);
    }

    public void updateModel(ECGModel ecgModel) {
        if (ecgModel == null) {
            throw new IllegalArgumentException("ecgModel must not be null"); //$NON-NLS-1$
        }

        // a new source console switches of dispose mode, because only not disposed consoles can be selected
        if (this.ecgModel != null && this.ecgModel.getSource() != ecgModel.getSource()) {
        	ecgModel.setSourceDisposed(false);
        }
        this.ecgModel = ecgModel;

        // Set the name to include our grep expression and the source console
        String sourceName = cut(ecgModel.getSource().getName(), SOURCE_CONSOLE_NAME_LENGTH);
        String disposeString = ""; //$NON-NLS-1$
        if (ecgModel.isSourceDisposed()) {
            disposeString = Messages.GrepConsole_SourceConsoleDisposed;
        }
        setName(disposeString + Activator.GREP_CONSOLE_NAME + LINE_NUMBER_SEPARATOR
                + Messages.GrepConsole_Watching + " [" + sourceName + "] "  //$NON-NLS-1$//$NON-NLS-2$
                + (ecgModel.isNotMatching() ? Messages.GrepConsole_NotMatching : Messages.GrepConsole_Matching) + " \"" //$NON-NLS-1$
                + cut(ecgModel.getSearchString(), SEARCH_STRING_LENGTH) + "\"" //$NON-NLS-1$ //$NON-NLS-2$
                + (ecgModel.isRangeMatching() ? " --> \"" + cut(ecgModel.getSearchEndString(), SEARCH_STRING_LENGTH) + "\"" : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " (" + (ecgModel.isCaseSensitive() ? Messages.GrepConsole_CaseSensitive : Messages.GrepConsole_IgnoreCase) + " " //$NON-NLS-1$ //$NON-NLS-2$
                + (ecgModel.isRegularExpression() ? Messages.GrepConsole_AsGrepExp : Messages.GrepConsole_AsString) + "" //$NON-NLS-1$
                + (ecgModel.isWholeWord() ? " whole word" : "") + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


        int paramInput = 0;
        if (!ecgModel.isCaseSensitive()) {
            paramInput = Pattern.CASE_INSENSITIVE;
        }

        String searchString = ecgModel.getSearchString();
        String endString = ecgModel.getSearchEndString();
        if (ecgModel.isRangeMatching()) {
            rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
        }


        if (!ecgModel.isRegularExpression()) {
            searchString = Pattern.quote(searchString);
            if (ecgModel.isRangeMatching()) {
                endString = Pattern.quote(endString);

            }
        }
        if (ecgModel.isWholeWord()) {
            searchString = "\\b" + searchString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$
            if (ecgModel.isRangeMatching()) {
                endString = "\\b" + endString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        grepPattern = Pattern.compile(searchString, paramInput);
        if (ecgModel.isRangeMatching()) {
            grepPatternEnd = Pattern.compile(endString, paramInput);
        }

        setSource(ecgModel.getSource());

    }

    public void writeToConsole(String line) {
        _styleRangeCache.put(grepDocumentLenth, createStyle(grepDocumentLenth, line));

        try {
            line = line.replaceAll(lineSeperator, "") + lineSeperator; //$NON-NLS-1$
            grepDocumentLenth += line.length();
            outStream.write(line);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private StyleRange[] createStyle(int lineOffset, String lineText) {
        boolean showLineOffset = Activator.getDefault().getPreferenceStore()
                .getBoolean(Activator.PREF_SHOW_LINE_OFFSET);
        boolean highlightMatched = Activator.getDefault().getPreferenceStore()
                .getBoolean(Activator.PREF_HIGHLIGHT_MATCHES);

        List<StyleRange> styles = new ArrayList<StyleRange>();
        if (lineText.startsWith(Activator.GREP_CONSOLE_OUTPUT_PREFIX)) {
            styles.add(new StyleRange(lineOffset, lineText.length(),
            		getColor(Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND),
            		null, SWT.ITALIC));
        } else {
            int lineNumberLength = 0;
            Color lineNumberForegrund = getColor(Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND);
            int lineNumberStyle = SWT.ITALIC;
            if (showLineOffset) {
                lineNumberLength = lineText.indexOf(LINE_NUMBER_SEPARATOR);
                if (lineNumberLength != -1) {
                    String possibleLineNumber = lineText.substring(0, lineNumberLength);
                    try {
                        int lineNumber = Integer.parseInt(possibleLineNumber);
                        if (lineNumber - prevLineNumber > 1 || lineNumber - prevLineNumber < 0) {
                        	lineNumberForegrund = getColor(Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND_DARKER);
                        	lineNumberStyle = SWT.ITALIC | SWT.BOLD;
                        }

                        styles.add(new StyleRange(lineOffset, lineNumberLength + 1, lineNumberForegrund, null, lineNumberStyle));

                        prevLineNumber = lineNumber;
                    } catch (NumberFormatException e) {
                        // don't color non-numbers
                    }
                }
            }
            if (highlightMatched) {
                boolean matchDrawn = setMatchHighlighting(styles, lineText, lineOffset,
                		(showLineOffset ? lineNumberLength + LINE_NUMBER_SEPARATOR.length() : 0));

                if (!matchDrawn && showLineOffset && highlightMatched) {
                	styles.add(new StyleRange(lineOffset, lineNumberLength,
                			getColor(Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND_LIGHTER), null, lineNumberStyle));
                }
            }
        }

        StyleRange[] styleRanges = (StyleRange[]) styles.toArray(new StyleRange[styles.size()]);
        return styleRanges;
    }


    private int getSubsequentLines() {
        return Math.max(0, Activator.getDefault().getPreferenceStore().getInt(Activator.PREF_SUBSEQUENT_LINES));
    }

    private int getConsoleWasSilentTimeRange() {
        return Math.max(0, Activator.getDefault().getPreferenceStore().getInt(Activator.PREF_CONSOLE_WAS_SILENT_FOR));
    }

    private boolean isMatching(String compareLine) {

        boolean returnValue = false;

        if (ecgModel.isRangeMatching()) {

        	// correct previous state
        	if (rangeMatchingState == RangeMatchingState.END_REACHED) {
        		rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
        	}

            // check for empty start match
            if (ecgModel.getSearchString().isEmpty()) {
            	// start from invinity
            	if (endReached) {
                    rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
                }
            	else {
            		rangeMatchingState = RangeMatchingState.IN_RANGE;
            	}
            }
            else {
            	// check for start match
            	if (grepPattern.matcher(compareLine).find()) {
            		rangeMatchingState = RangeMatchingState.IN_RANGE;
            	}
            }

            // check for end match
            if (ecgModel.getSearchEndString().isEmpty()) {
            	// end to invinity
            }
            else {
	            if (grepPatternEnd.matcher(compareLine).find()) {
	            	if (ecgModel.getSearchString().isEmpty()) {
	            		endReached = true;
	            	}
	            	if (rangeMatchingState == RangeMatchingState.IN_RANGE) {
	            		rangeMatchingState = RangeMatchingState.END_REACHED;
	            	}
	            	else {
	            		rangeMatchingState = RangeMatchingState.END_MATCH;
	            	}
	            	returnValue = true;
	            }
            }
            // from start match until end match inclusive end reached, return true!
            if (rangeMatchingState == RangeMatchingState.IN_RANGE) {
                returnValue = true;
            }
        }
        else {
            if (grepPattern.matcher(compareLine).find()) {
                returnValue = true;
            }
        }


        if (ecgModel.isNotMatching()) {
            returnValue = !returnValue;
        }

        // reset count because there is a match
        if (returnValue == true) {
            subsequentLineCounter = getSubsequentLines();
        }

        if (oldMatching && subsequentLineCounter > 0) {
            // decrease pre number count because there is no match
            if (returnValue == false) {
                subsequentLineCounter--;
            }
            return true;
        }
        oldMatching = returnValue;

        return returnValue;
    }

    private boolean setMatchHighlighting(List<StyleRange> styles, String compareLine, int lineOffset, int prefixLength) {

        ECGModel ecgModel = getModel();

        String searchString = ecgModel.getSearchString();

        if (!ecgModel.isCaseSensitive()) {
            compareLine = compareLine.toLowerCase();
            searchString = searchString.toLowerCase();
        }


        boolean blockDrawn = false; // to avoid a StyleRange - bug
        boolean matchDrawn = false;
        if (ecgModel.isRangeMatching()) {
            // block
            if (rangeMatchingState == RangeMatchingState.IN_RANGE || rangeMatchingState == RangeMatchingState.END_REACHED) {
                styles.add(new StyleRange(lineOffset + prefixLength, compareLine.length() - prefixLength,
                		getColor(Activator.PREF_COLOR_MATCH_FOREGROUND_RANGE),
                		getColor(Activator.PREF_COLOR_MATCH_BACKGROUND_RANGE), SWT.NONE));
                blockDrawn = true;
            }

            // starting point
            if (!ecgModel.getSearchString().isEmpty()) {
            	Matcher matcher = grepPattern.matcher(compareLine.substring(prefixLength));
	            while (matcher.find()) {
	            	styles.add(new StyleRange(lineOffset + prefixLength + matcher.start(),
	            			Math.max(0, matcher.end() - matcher.start() -(blockDrawn ? 1: 0)),
	            			getColor(Activator.PREF_COLOR_MATCH_FOREGROUND),
	            			getColor(Activator.PREF_COLOR_MATCH_BACKGROUND), SWT.NONE));
	            	matchDrawn = true;
	            }
            }
            // end point
            if (!ecgModel.getSearchEndString().isEmpty() &&
            		(rangeMatchingState == RangeMatchingState.END_REACHED || rangeMatchingState == RangeMatchingState.END_MATCH)) {
                Matcher matcher = grepPatternEnd.matcher(compareLine.substring(prefixLength));
                while (matcher.find()) {
                    styles.add(new StyleRange(lineOffset + prefixLength + matcher.start(),
                    		Math.max(0, matcher.end() - matcher.start() -(blockDrawn ? 1: 0)),
                    		getColor(Activator.PREF_COLOR_MATCH_FOREGROUND),
                    		getColor(Activator.PREF_COLOR_MATCH_BACKGROUND), SWT.NONE)); // -1 is cause of an eclipse bug
                    matchDrawn = true;
                }
            }
        }
        else {
        	// single match
        	Matcher matcher = grepPattern.matcher(compareLine.substring(prefixLength));
        	while (matcher.find()) {
        		styles.add(new StyleRange(lineOffset + prefixLength + matcher.start(), matcher.end() - matcher.start(),
        				getColor(Activator.PREF_COLOR_MATCH_FOREGROUND),
        				getColor(Activator.PREF_COLOR_MATCH_BACKGROUND), SWT.NONE));
        		matchDrawn = true;
        	}

        }

       return matchDrawn | blockDrawn;

    }

    private void writeToConsole(int lineNumber, String line) {

        boolean showLineOffset = Activator.getDefault().getPreferenceStore()
                .getBoolean(Activator.PREF_SHOW_LINE_OFFSET);

        if (showLineOffset) {
            line = lineNumber + LINE_NUMBER_SEPARATOR + line;
        }
        writeToConsole(line);
    }

    @Override
    protected void dispose() {
        ECGContext.getECGMap().remove(this);
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (watchedDoc != null) {
            watchedDoc.removeDocumentListener(this);
        }

        super.dispose();
    }

    public void resetRange() {
    	rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
    	endReached = false;

    }

    private Color getColor(String name) {
    	RGB rgb = PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(), name);

    	final String rgbKey = Activator.GREP_CONSOLE_NAME + "_" + rgb.toString() ; //$NON-NLS-1$
		Color color = JFaceResources.getColorRegistry().get(rgbKey);
		if (color == null || color.isDisposed()) {
			JFaceResources.getColorRegistry().put(rgbKey, rgb);
			color = JFaceResources.getColorRegistry().get(rgbKey);
		}

		return color;
    }


    private String cut(String s, int count) {
    	if (s.length() > count) {
            return s.substring(0, count - 3) + "..."; //$NON-NLS-1$
        }
    	return s;
    }

}
