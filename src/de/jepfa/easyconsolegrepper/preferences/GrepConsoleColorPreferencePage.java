package de.jepfa.easyconsolegrepper.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.nls.Messages;

public class GrepConsoleColorPreferencePage  extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

public static final String PREF_PAGE_ID ="de.jepfa.easyconsolegrepper.preferences.GrepConsoleColorPreferencePage"; //$NON-NLS-1$

	public GrepConsoleColorPreferencePage() {
	        super(GRID);
	        setPreferenceStore(Activator.getDefault().getPreferenceStore());
	        setDescription("Configure here the colors of " + Activator.GREP_CONSOLE_NAME); //$NON-NLS-1$
	}

	public void createFieldEditors() {
		addField(new ColorFieldEditor(Activator.PREF_COLOR_MATCH_FOREGROUND, Messages.GrepConsolePreferencePage_COLOR_MATCH_FOREGROUND, getFieldEditorParent()));
		addField(new ColorFieldEditor(Activator.PREF_COLOR_MATCH_BACKGROUND, Messages.GrepConsolePreferencePage_COLOR_MATCH_BACKGROUND, getFieldEditorParent()));
		addField(new ColorFieldEditor(Activator.PREF_COLOR_MATCH_FOREGROUND_RANGE, Messages.GrepConsolePreferencePage_COLOR_MATCH_FOREGROUND_RANGE, getFieldEditorParent()));
		addField(new ColorFieldEditor(Activator.PREF_COLOR_MATCH_BACKGROUND_RANGE, Messages.GrepConsolePreferencePage_COLOR_MATCH_BACKGROUND_RANGE, getFieldEditorParent()));
		addField(new ColorFieldEditor(Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND_DARKER, Messages.GrepConsolePreferencePage_COLOR_LINE_NUMBER_FOREGROUND_DIFF, getFieldEditorParent()));
		addField(new ColorFieldEditor(Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND, Messages.GrepConsolePreferencePage_COLOR_LINE_NUMBER_FOREGROUND, getFieldEditorParent()));
		addField(new ColorFieldEditor(Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND_LIGHTER, Messages.GrepConsolePreferencePage_COLOR_LINE_NUMBER_FOREGROUND_OTHERS, getFieldEditorParent()));

	}

	public void init(IWorkbench workbench) {
	}
}

