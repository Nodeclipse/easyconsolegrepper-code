package de.jepfa.easyconsolegrepper.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.jepfa.easyconsolegrepper.nls.messages"; //$NON-NLS-1$
	public static String Activator_PRODUCT_NAME;
	public static String ChangeSettingsAction_ChangeCurrentFilterSettings;
	public static String ChangeSettingsAction_ChangeSettings;
	public static String ConsoleConfigDialog_CaseSensitive;
	public static String ConsoleConfigDialog_ContainingText;
	public static String ConsoleConfigDialog_ContainingRegexp;
	public static String ConsoleConfigDialog_CreateNewGrepConsole;
	public static String ConsoleConfigDialog_EditCurrentConsole;
	public static String ConsoleConfigDialog_EndExpression;
	public static String ConsoleConfigDialog_RangeMatching;
	public static String ConsoleConfigDialog_NewConsole;
	public static String ConsoleConfigDialog_NoTextSourceConsoleSelected;
	public static String ConsoleConfigDialog_NotMatching;
	public static String ConsoleConfigDialog_RegularExpression;
	public static String ConsoleConfigDialog_RemoveEntries;
	public static String ConsoleConfigDialog_SourceConsole;
	public static String ConsoleConfigDialog_StartExpession;
	public static String ConsoleConfigDialog_WholeWord;
	public static String HandlerUtil_CloseDisposedSourceConsoleQuestion;
	public static String HandlerUtil_DontAskAgain;
	public static String ConsoleConfigHandler_NoTextConsolesFound;
	public static String GrepConsole_AsGrepExp;
	public static String GrepConsole_AsString;
	public static String GrepConsole_CaseSensitive;
	public static String GrepConsole_IgnoreCase;
	public static String GrepConsole_Matching;
	public static String GrepConsole_NotMatching;
	public static String GrepConsole_SourceConsoleDisposed;
	public static String GrepConsole_SourceConsoleWasSilent;
	public static String GrepConsole_Watching;
	public static String GrepConsolePreferencePage_ActivateAfterResuming;
	public static String GrepConsolePreferencePage_COLOR_LINE_NUMBER_FOREGROUND;
	public static String GrepConsolePreferencePage_COLOR_LINE_NUMBER_FOREGROUND_DIFF;
	public static String GrepConsolePreferencePage_COLOR_LINE_NUMBER_FOREGROUND_OTHERS;
	public static String GrepConsolePreferencePage_COLOR_MATCH_BACKGROUND;
	public static String GrepConsolePreferencePage_COLOR_MATCH_BACKGROUND_RANGE;
	public static String GrepConsolePreferencePage_COLOR_MATCH_FOREGROUND;
	public static String GrepConsolePreferencePage_COLOR_MATCH_FOREGROUND_RANGE;
	public static String GrepConsolePreferencePage_ConfigureTheBehaviorOfXX;
	public static String GrepConsolePreferencePage_CountOfSubsequentLines;
	public static String GrepConsolePreferencePage_DontAskBeforeDisposedAction;
	public static String GrepConsolePreferencePage_HighlightMatches;
	public static String GrepConsolePreferencePage_ResumeAfterDisposed;
	public static String GrepConsolePreferencePage_ShowSourceLine;
	public static String GrepConsolePreferencePage_TimeSecondsAfterConsoleSilence;
	public static String RegrepSourceInputAction_RegrepCompleteSource;
	public static String RegrepSourceInputAction_RegrepCompleteSourceTooltip;
	public static String SwitchFilterOnOffAction_FilterOnOff;
	public static String SwitchFilterOnOffAction_ToolTip_FilterOff;
	public static String SwitchFilterOnOffAction_ToolTip_FilterOn;
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
