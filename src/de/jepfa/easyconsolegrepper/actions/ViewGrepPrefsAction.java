package de.jepfa.easyconsolegrepper.actions;

import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.nls.Messages;
import de.jepfa.easyconsolegrepper.preferences.GrepConsolePreferencePage;

/**
 * Action for providing preferences for Grep Console settings.
 *  
 * @author Jens Pfahl
 */
public class ViewGrepPrefsAction extends Action {
	
	public ViewGrepPrefsAction() {
		 super(Messages.ViewGrepPrefsAction_Show_Prefs, Activator.getImageDescriptor(Activator.IMAGE_PENCIL_16));
	        setToolTipText(MessageFormat.format(Messages.ViewGrepPrefsAction_Tooltip_Show_Prefs, Messages.Activator_PRODUCT_NAME)); 
	}
	
	@Override
	public void run() {
		PreferenceDialog pref =
				PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						GrepConsolePreferencePage.PREF_PAGE_ID, null, null);
				if (pref != null)
				pref.open();

	}

}
