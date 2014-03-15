package de.jepfa.easyconsolegrepper.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import de.jepfa.easyconsolegrepper.internal.Activator;

public class GrepConsoleColorPreferenceInitializer extends
		AbstractPreferenceInitializer {


	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		PreferenceConverter.setDefault(store, Activator.PREF_COLOR_MATCH_FOREGROUND, Display.getCurrent().getSystemColor(SWT.COLOR_BLACK).getRGB());
        PreferenceConverter.setDefault(store, Activator.PREF_COLOR_MATCH_BACKGROUND, Display.getCurrent().getSystemColor(SWT.COLOR_GRAY).getRGB());
        PreferenceConverter.setDefault(store, Activator.PREF_COLOR_MATCH_FOREGROUND_RANGE, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE).getRGB());
        PreferenceConverter.setDefault(store, Activator.PREF_COLOR_MATCH_BACKGROUND_RANGE, new RGB(230, 250, 255));
        PreferenceConverter.setDefault(store, Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN).getRGB());
        PreferenceConverter.setDefault(store, Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND_DARKER, new RGB(0, 100, 100));
        PreferenceConverter.setDefault(store, Activator.PREF_COLOR_LINE_NUMBER_FOREGROUND_LIGHTER, new RGB(150, 200, 200));

	}

}
