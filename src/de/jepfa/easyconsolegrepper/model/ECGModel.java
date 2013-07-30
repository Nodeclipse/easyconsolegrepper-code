package de.jepfa.easyconsolegrepper.model;

import org.eclipse.ui.console.TextConsole;

/**
 * Bean for one Grep Console.
 * 
 * @author Jens Pfahl
 */
public class ECGModel {

	private TextConsole source;
	private String searchString = ""; //$NON-NLS-1$
	private String sourceName = ""; //$NON-NLS-1$
	private boolean caseSensitive = false;
	private boolean regularExpression = false;
	private boolean notMatching = false;
	private boolean sourceDisposed = false;


	public TextConsole getSource() {
		return source;
	}

	public void setSource(TextConsole source) {
		this.source = source;
		setSourceName(source.getName());
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isRegularExpression() {
		return regularExpression;
	}

	public void setRegularExpression(boolean regularExpression) {
		this.regularExpression = regularExpression;
	}

	public boolean isSourceDisposed() {
		return sourceDisposed;
	}

	public void setSourceDisposed(boolean sourceDisposed) {
		this.sourceDisposed = sourceDisposed;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public boolean isNotMatching() {
		return notMatching;
	}

	public void setNotMatching(boolean notMatching) {
		this.notMatching = notMatching;
	}
	

}
