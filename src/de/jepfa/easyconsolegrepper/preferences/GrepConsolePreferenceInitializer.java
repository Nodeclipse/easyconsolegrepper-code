package de.jepfa.easyconsolegrepper.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.jepfa.easyconsolegrepper.internal.Activator;

public class GrepConsolePreferenceInitializer extends
		AbstractPreferenceInitializer {
	

	/*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            store.setDefault(Activator.PREF_RESUME_TERMINATED_CONSOLE, false);
            store.setDefault(Activator.PREF_ACTIVATE_CONSOLE_ON_RESUMING, false);
            store.setDefault(Activator.PREF_SHOW_LINE_OFFSET, true);
            store.setDefault(Activator.PREF_HIGHLIGHT_MATCHES, true);
            store.setDefault(Activator.PREF_SUBSEQUENT_LINES, 0);
          
    }


}
