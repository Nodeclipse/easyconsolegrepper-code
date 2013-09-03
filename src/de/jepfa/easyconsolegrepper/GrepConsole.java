package de.jepfa.easyconsolegrepper;

import java.io.IOException;
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

	private static final int SOURCE_CONSOLE_NAME_LENGTH = 40;
	private static final Color MATCH_BACKGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	private static final Color MATCH_FOREGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	private static final Color LINE_NUMBER_FOREGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
	private static final Color LINE_NUMBER_FOREGROUND_DARKER =  new Color(null, 0, 100, 100);

	/**
	 * Key = lini offset
	 * Value = Styles of this line
	 */
	private Map<Integer, StyleRange[]> _styleRangeCache = new ConcurrentHashMap<Integer, StyleRange[]>(); 

	private IOConsoleOutputStream outStream = newOutputStream();
	private int grepDocumentLenth = 0;
	private IDocument watchedDoc;
	private String lineSeperator = "\n"; //$NON-NLS-1$
	private Pattern grepPattern;
	private String lineBuff = ""; //$NON-NLS-1$
	private ECGModel ecgModel;
	private int subsequentLineCounter = getSubsequentLines();
	private boolean oldMatching = false;
	private int prevLineNumber;

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

			if (isMatching(line)) {
				writeToConsole(lineNumber, line);
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
		String sourceName = ecgModel.getSource().getName();
		if (sourceName.length() > SOURCE_CONSOLE_NAME_LENGTH) {
			sourceName = sourceName.substring(0, SOURCE_CONSOLE_NAME_LENGTH - 3) + "..."; //$NON-NLS-1$
		}
		String disposeString = ""; //$NON-NLS-1$
		if (ecgModel.isSourceDisposed()) {
			disposeString = "*" + Messages.GrepConsole_SourceConsoleDisposed + "* "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		setName(disposeString + Activator.GREP_CONSOLE_NAME + ": " //$NON-NLS-1$
				+ Messages.GrepConsole_Watching + " [" + sourceName + "] "  //$NON-NLS-1$//$NON-NLS-2$ 
				+ (ecgModel.isNotMatching() ? Messages.GrepConsole_NotMatching : Messages.GrepConsole_Matching) + " \"" + ecgModel.getSearchString() //$NON-NLS-1$ 
				+ "\" (" + (ecgModel.isCaseSensitive() ? Messages.GrepConsole_CaseSensitive : Messages.GrepConsole_IgnoreCase) + " " //$NON-NLS-1$ //$NON-NLS-2$ 
				+ (ecgModel.isRegularExpression() ? Messages.GrepConsole_AsGrepExp : Messages.GrepConsole_AsString) + ")"); //$NON-NLS-1$

		// Compile our grep regex
		if (ecgModel.isRegularExpression()) {
			if (!ecgModel.isCaseSensitive()) {
				grepPattern = Pattern.compile(ecgModel.getSearchString(), Pattern.CASE_INSENSITIVE);
			} else {
				grepPattern = Pattern.compile(ecgModel.getSearchString());
			}
		} else {
			grepPattern = null;
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
						if (lineNumber - prevLineNumber > 1) {
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

	private boolean isMatching(String compareLine) {

		boolean returnValue = false;

		String searchString = ecgModel.getSearchString();

		if (!ecgModel.isCaseSensitive()) {
			compareLine = compareLine.toLowerCase();
			searchString = searchString.toLowerCase();
		}

		if (ecgModel.isRegularExpression()) {
			if (grepPattern.matcher(compareLine).find()) {
				returnValue = true;
			}
		} else {
			if (searchString.equals("") || compareLine.indexOf(searchString) >= 0) { //$NON-NLS-1$
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

		if (ecgModel.isRegularExpression()) {
			Matcher matcher = grepPattern.matcher(compareLine.substring(lineNumberLength));
			while (matcher.find()) {
				styles.add(new StyleRange(lineOffset + lineNumberLength + matcher.start(), matcher.end()
						- matcher.start(), MATCH_FOREGROUND, MATCH_BACKGROUND, SWT.NONE));
			}
		} else if (!searchString.equals("")) { //$NON-NLS-1$
			int index = compareLine.indexOf(searchString, lineNumberLength);
			if (index > 0) {
				do {
					styles.add(new StyleRange(lineOffset + index, searchString.length(), MATCH_FOREGROUND,
							MATCH_BACKGROUND, SWT.NONE));
				} while ((index = compareLine.indexOf(searchString, index + searchString.length())) > 0);
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

}
