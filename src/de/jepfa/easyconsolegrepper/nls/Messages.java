package de.jepfa.easyconsolegrepper.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.jepfa.easyconsolegrepper.nls.messages"; //$NON-NLS-1$
	public static String Activator_PRODUCT_NAME;
	public static String ChangeSettingsAction_ChangeCurrentFilterSettings;
	public static String ChangeSettingsAction_ChangeSettings;
	public static String ConsoleConfigDialog_CaseSensitive;
	public static String ConsoleConfigDialog_ContainingText;
	public static String ConsoleConfigDialog_CreateNewGrepConsole;
	public static String ConsoleConfigDialog_EditCurrentConsole;
	public static String ConsoleConfigDialog_NewConsole;
	public static String ConsoleConfigDialog_NoTextSourceConsoleSelected;
	public static String ConsoleConfigDialog_NotMatching;
	public static String ConsoleConfigDialog_RegularExpression;
	public static String ConsoleConfigDialog_RemoveEntries;
	public static String ConsoleConfigDialog_SourceConsole;
	public static String ConsoleConfigHandler_CloseDisposedSourceConsoleQuestion;
	public static String ConsoleConfigHandler_NoTextConsolesFound;
	public static String GrepConsole_AsGrepExp;
	public static String GrepConsole_AsString;
	public static String GrepConsole_CaseSensitive;
	public static String GrepConsole_IgnoreCase;
	public static String GrepConsole_Matching;
	public static String GrepConsole_NotMatching;
	public static String GrepConsole_SourceConsoleDisposed;
	public static String GrepConsole_Watching;
	public static String GrepConsolePreferencePage_ActivateAfterResuming;
	public static String GrepConsolePreferencePage_ConfigureTheBehaviorOfXX;
	public static String GrepConsolePreferencePage_CountOfSubsequentLines;
	public static String GrepConsolePreferencePage_HighlightMatches;
	public static String GrepConsolePreferencePage_ResumeAfterDisposed;
	public static String GrepConsolePreferencePage_ShowSourceLine;
	public static String RegrepHandler_CloseDisposedSourceConsoleQuestion;
	public static String RegrepSourceInputAction_RegrepCompleteSource;
	public static String RegrepSourceInputAction_RegrepCompleteSourceTooltip;
	public static String TextConsolePageParticipant_ResumingConsoleGrepping;
	public static String ViewGrepPrefsAction_Show_Prefs;
	public static String ViewGrepPrefsAction_Tooltip_Show_Prefs;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
