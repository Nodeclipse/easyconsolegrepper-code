package de.jepfa.easyconsolegrepper;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
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

    private enum RangeMatchingState {IN_RANGE, END_REACHED, NOT_IN_RANGE, END_POINT};

    private static final int SOURCE_CONSOLE_NAME_LENGTH = 40;
    private static final int SEARCH_STRING_LENGTH = 30;
    private static final Color MATCH_BACKGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
    private static final Color MATCH_BACKGROUND2 = new Color(null, 230, 250, 255);
    private static final Color MATCH_FOREGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    private static final Color MATCH_FOREGROUND2 = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
    private static final Color LINE_NUMBER_FOREGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
    private static final Color LINE_NUMBER_FOREGROUND_DARKER = new Color(null, 0, 100, 100);

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
        this.ecgModel = ecgModel;

        // Set the name to include our grep expression and the source console
        String sourceName = cut(ecgModel.getSource().getName(), SOURCE_CONSOLE_NAME_LENGTH);
        String disposeString = ""; //$NON-NLS-1$
        if (ecgModel.isSourceDisposed()) {
            disposeString = "*" + Messages.GrepConsole_SourceConsoleDisposed + "* "; //$NON-NLS-1$ //$NON-NLS-2$
        }
        String searchString = cut(ecgModel.getSearchString(), SEARCH_STRING_LENGTH);
        String searchEndString = cut(ecgModel.getSearchEndString(), SEARCH_STRING_LENGTH);
        setName(disposeString + Activator.GREP_CONSOLE_NAME + ": " //$NON-NLS-1$
                + Messages.GrepConsole_Watching + " [" + sourceName + "] "  //$NON-NLS-1$//$NON-NLS-2$
                + (ecgModel.isNotMatching() ? Messages.GrepConsole_NotMatching : Messages.GrepConsole_Matching) + " \"" + searchString + "\"" //$NON-NLS-1$ //$NON-NLS-2$
                + (ecgModel.isLineMatching() ? " --> \"" + searchEndString + "\"" : "") //$NON-NLS-1$ //$NON-NLS-2$
                + " (" + (ecgModel.isCaseSensitive() ? Messages.GrepConsole_CaseSensitive : Messages.GrepConsole_IgnoreCase) + " " //$NON-NLS-1$ //$NON-NLS-2$
                + (ecgModel.isRegularExpression() ? Messages.GrepConsole_AsGrepExp : Messages.GrepConsole_AsString) + "" //$NON-NLS-1$
                + (ecgModel.isWholeWord() ? " whole word" : "") + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


        int paramInput = 0;
        if (!ecgModel.isCaseSensitive()) {
            paramInput = Pattern.CASE_INSENSITIVE;
        }

        String endString = null;
        if (ecgModel.isLineMatching()) {
            endString = ecgModel.getSearchEndString();
            rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
        }


        if (!ecgModel.isRegularExpression()) {
            searchString = Pattern.quote(searchString);
            if (ecgModel.isLineMatching()) {
                endString = Pattern.quote(endString);

            }
        }
        if (ecgModel.isWholeWord()) {
            searchString = "\\b" + searchString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$
            if (ecgModel.isLineMatching()) {
                endString = "\\b" + endString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        grepPattern = Pattern.compile(searchString, paramInput);
        if (ecgModel.isLineMatching()) {
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
            styles.add(new StyleRange(lineOffset, lineText.length(), LINE_NUMBER_FOREGROUND, null, SWT.ITALIC));
        } else {
            int lineNumberLength = 0;
            if (showLineOffset) {
                lineNumberLength = lineText.indexOf(": "); //$NON-NLS-1$
                if (lineNumberLength != -1) {
                    String possibleLineNumber = lineText.substring(0, lineNumberLength);
                    try {
                        int lineNumber = Integer.parseInt(possibleLineNumber);
                        if (lineNumber - prevLineNumber > 1 || lineNumber - prevLineNumber < 0) {
                            styles.add(new StyleRange(lineOffset, lineNumberLength + 1, LINE_NUMBER_FOREGROUND_DARKER, null,
                                    SWT.ITALIC | SWT.BOLD));
                        }
                        else {
                            styles.add(new StyleRange(lineOffset, lineNumberLength + 1, LINE_NUMBER_FOREGROUND, null,
                                    SWT.ITALIC));
                        }
                        prevLineNumber = lineNumber;
                    } catch (NumberFormatException e) {
                        // don't color non-numbers
                    }
                }
            }
            if (highlightMatched) {
                setMatchHighlighting(styles, lineText, lineOffset, lineNumberLength + 1);
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

        if (ecgModel.isLineMatching()) {
            // reset previous state
            if (rangeMatchingState == RangeMatchingState.END_REACHED) {
                rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
            }
            // check for start match
            if (grepPattern.matcher(compareLine).find()) {
                rangeMatchingState = RangeMatchingState.IN_RANGE;
            }
            // check for end match
            if (grepPatternEnd.matcher(compareLine).find()) {
            	if (rangeMatchingState == RangeMatchingState.IN_RANGE) {
            		rangeMatchingState = RangeMatchingState.END_REACHED;
            	}
            	else {
            		rangeMatchingState = RangeMatchingState.END_POINT;
            	}
            	returnValue = true; // zwischenzeilen
            }
            // from start match until end match inclusive end reached, return true!
            if (rangeMatchingState == RangeMatchingState.IN_RANGE) {
                returnValue = true; // zwischenzeilen
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

    private void setMatchHighlighting(List<StyleRange> styles, String compareLine, int lineOffset, int lineNumberLength) {

        ECGModel ecgModel = getModel();

        String searchString = ecgModel.getSearchString();

        if (!ecgModel.isCaseSensitive()) {
            compareLine = compareLine.toLowerCase();
            searchString = searchString.toLowerCase();
        }


        boolean blockDrawn = false; // to avoid a StyleRange - bug
        if (ecgModel.isLineMatching()) {
            // block
            if (rangeMatchingState == RangeMatchingState.IN_RANGE || rangeMatchingState == RangeMatchingState.END_REACHED) {
                styles.add(new StyleRange(lineOffset + lineNumberLength, compareLine.length() - lineNumberLength,
                        MATCH_FOREGROUND2, MATCH_BACKGROUND2, SWT.NONE));
                blockDrawn = true;
            }

            // starting point
            Matcher matcher = grepPattern.matcher(compareLine.substring(lineNumberLength));
            while (matcher.find()) {
            	styles.add(new StyleRange(lineOffset + lineNumberLength + matcher.start(), 
            			Math.max(0, matcher.end() - matcher.start() -(blockDrawn ? 1: 0)), MATCH_FOREGROUND, MATCH_BACKGROUND, SWT.NONE));
            }
            // end point
            if (rangeMatchingState == RangeMatchingState.END_REACHED || rangeMatchingState == RangeMatchingState.END_POINT) {
                matcher = grepPatternEnd.matcher(compareLine.substring(lineNumberLength));
                while (matcher.find()) {
                    styles.add(new StyleRange(lineOffset + lineNumberLength + matcher.start(), 
                    		Math.max(0, matcher.end() - matcher.start() -(blockDrawn ? 1: 0)), MATCH_FOREGROUND, MATCH_BACKGROUND, SWT.NONE)); // -1 is cause of an eclipse bug
                }
            }
        }
        else {
        	// single match
        	Matcher matcher = grepPattern.matcher(compareLine.substring(lineNumberLength));
        	while (matcher.find()) {
        		styles.add(new StyleRange(lineOffset + lineNumberLength + matcher.start(), matcher.end()
        				- matcher.start(), MATCH_FOREGROUND, MATCH_BACKGROUND, SWT.NONE));
        	}
        	
        }


    }

    private void writeToConsole(int lineNumber, String line) {

        boolean showLineOffset = Activator.getDefault().getPreferenceStore()
                .getBoolean(Activator.PREF_SHOW_LINE_OFFSET);

        if (showLineOffset) {
            line = lineNumber + ": " + line; //$NON-NLS-1$
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
    
    public void reset() {
    	rangeMatchingState = RangeMatchingState.NOT_IN_RANGE;
    }

    private String cut(String s, int count) {
    	if (s.length() > count) {
            return s.substring(0, count - 3) + "..."; //$NON-NLS-1$
        }
    	return s;
    }

}
