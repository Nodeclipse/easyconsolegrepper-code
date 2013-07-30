package de.jepfa.easyconsolegrepper.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

import de.jepfa.easyconsolegrepper.ConsoleConfigDialog;
import de.jepfa.easyconsolegrepper.GrepConsole;
import de.jepfa.easyconsolegrepper.GrepConsoleFactory;
import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.model.ECGModel;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * Handler for opening {@link ConsoleConfigDialog}.
 * 
 * @author Jens Pfahl
 */
public class ConsoleConfigHandler extends AbstractHandler {
	
	public static final String CMD_ID = "de.jepfa.easyconsolegrepper.handler.ConsoleConfigHandler"; //$NON-NLS-1$


	public Object execute(ExecutionEvent event) throws ExecutionException {
		run();
		return null;
	}


	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		if (consoles.length == 0) {
			MessageDialog.openError(shell, Activator.GREP_CONSOLE_NAME, Messages.ConsoleConfigHandler_NoTextConsolesFound);
			return;
		}
		
		IConsole activeConsole = GrepConsoleFactory.getActiveConsole();
		
		if (activeConsole instanceof GrepConsole) {
			GrepConsole grepConsole = (GrepConsole)activeConsole;
			ECGModel ecgModel = ECGContext.getECGMap().get(grepConsole);
			
			Assert.isNotNull(ecgModel);

			if (ecgModel.isSourceDisposed()) {
				boolean closeConsoleGrep = MessageDialog.openQuestion(shell, Activator.GREP_CONSOLE_NAME, 
						Messages.ConsoleConfigHandler_CloseDisposedSourceConsoleQuestion);
				if (closeConsoleGrep) {
					ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{grepConsole});
					ECGContext.getECGMap().remove(grepConsole);
					return;
				}
			}
			
			
			ConsoleConfigDialog consoleConfigDialog = new ConsoleConfigDialog(
					shell, ecgModel, false, consoles);
			int returnCode = consoleConfigDialog.open();
			if (returnCode == IDialogConstants.OK_ID) {
				// reset dispose state
				ecgModel.setSourceDisposed(false);
				// Update model
				ECGContext.getECGMap().put(grepConsole, ecgModel);
				// Update Grep Console
				grepConsole.updateModel(ecgModel);
			}

			
		}
		else {
			ECGModel ecgModel = new ECGModel();
			
			if (activeConsole != null 
					&& activeConsole instanceof TextConsole
					&& !(activeConsole instanceof GrepConsole)) {
				ecgModel.setSource((TextConsole)activeConsole);
			}


			// open grep config dialog
			ConsoleConfigDialog consoleConfigDialog = new ConsoleConfigDialog(
					shell, ecgModel, true, consoles);
			int returnCode = consoleConfigDialog.open();
			if (returnCode == IDialogConstants.OK_ID) {
				GrepConsoleFactory.createNewGrepConsole(consoleConfigDialog.getModel());
			}			
		}
	}
	
	

}
