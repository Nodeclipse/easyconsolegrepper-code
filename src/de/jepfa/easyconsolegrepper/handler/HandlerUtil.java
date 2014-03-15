package de.jepfa.easyconsolegrepper.handler;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;

import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.model.ECGModel;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * Helper for the handlers.
 *
 * @author Jens Pfahl
 */
public class HandlerUtil {

	public static Boolean checkDisposedActionAllowed(Shell shell, ECGModel ecgModel) {
		boolean showDisposeDialog = ! Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREF_DONT_SHOW_DISPOSAL_DIALOG);

		if (showDisposeDialog && ecgModel.isSourceDisposed()) {
			MessageDialogWithToggle res = MessageDialogWithToggle.openYesNoCancelQuestion(shell, Activator.GREP_CONSOLE_NAME,
					Messages.HandlerUtil_CloseDisposedSourceConsoleQuestion, Messages.HandlerUtil_DontAskAgain,
					false, null, null);
			if (res.getReturnCode() == MessageDialogWithToggle.CANCEL) {
				return null;
			}
			Activator.getDefault().getPreferenceStore().setValue(Activator.PREF_DONT_SHOW_DISPOSAL_DIALOG, res.getToggleState());

			if (res.getReturnCode() == IDialogConstants.YES_ID) {  // Window.OK has wrong value (0 instead of 2)
				return true;
			}
		}

		return false;
	}

}
