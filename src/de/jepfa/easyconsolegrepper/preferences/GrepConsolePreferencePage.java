package de.jepfa.easyconsolegrepper.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.nls.Messages;

public class GrepConsolePreferencePage  extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	public static final String PREF_PAGE_ID ="de.jepfa.easyconsolegrepper.preferences.GrepConsolePreferencePage"; //$NON-NLS-1$

	public GrepConsolePreferencePage() {
	        super(GRID);
	        setPreferenceStore(Activator.getDefault().getPreferenceStore());
	        setDescription(Messages.GrepConsolePreferencePage_ConfigureTheBehaviorOfXX + Activator.GREP_CONSOLE_NAME);
	}
	
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(Activator.PREF_SHOW_LINE_OFFSET, Messages.GrepConsolePreferencePage_ShowSourceLine, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Activator.PREF_HIGHLIGHT_MATCHES, Messages.GrepConsolePreferencePage_HighlightMatches, getFieldEditorParent()));
	    addField(new BooleanFieldEditor(Activator.PREF_RESUME_TERMINATED_CONSOLE, Messages.GrepConsolePreferencePage_ResumeAfterDisposed, getFieldEditorParent()));
	    addField(new BooleanFieldEditor(Activator.PREF_ACTIVATE_CONSOLE_ON_RESUMING, Messages.GrepConsolePreferencePage_ActivateAfterResuming, getFieldEditorParent()));
	    addField(new IntegerFieldEditor(Activator.PREF_CONSOLE_WAS_SILENT_FOR, Messages.GrepConsolePreferencePage_TimeSecondsAfterConsoleSilence, getFieldEditorParent()));
	    addField(new IntegerFieldEditor(Activator.PREF_SUBSEQUENT_LINES, Messages.GrepConsolePreferencePage_CountOfSubsequentLines, getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench) {
	}
}
