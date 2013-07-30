package de.jepfa.easyconsolegrepper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

/**
 * This class is responsible for formatting Grep Console output. The format style 
 * (an array of {@link StyleRange}) is created  from {@link GrepConsole} during
 * writing to the output stream and cached in
 * {@link GrepConsole#getCachedStyleRange(int, String)}
 * 
 * @author Jens Pfahl
 */
public class GrepConsoleStyleListener implements LineStyleListener {
	private final GrepConsole grepConsole;


	public GrepConsoleStyleListener(GrepConsole grepConsole, StyledText viewer) {
		this.grepConsole = grepConsole;

		viewer.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
	}

	public void lineGetStyle(LineStyleEvent event) {
		if (event.lineText.length() == 0) {
			return;
		}
		
		StyleRange[] usedStyleRanges = grepConsole.getCachedStyleRange(event.lineOffset);
		if (usedStyleRanges != null) {
			event.styles = usedStyleRanges;
		}
	}

}
