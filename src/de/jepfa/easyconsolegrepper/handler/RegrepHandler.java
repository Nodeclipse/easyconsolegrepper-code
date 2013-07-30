package de.jepfa.easyconsolegrepper.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import de.jepfa.easyconsolegrepper.GrepConsole;
import de.jepfa.easyconsolegrepper.GrepConsoleFactory;
import de.jepfa.easyconsolegrepper.actions.RegrepSourceInputAction;
import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.model.ECGModel;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * Handler for opening {@link RegrepSourceInputAction}
 * 
 * @author Jens Pfahl
 */
public class RegrepHandler extends AbstractHandler {
	
	public static final String CMD_ID = "de.jepfa.easyconsolegrepper.handler.RegexpHandler"; //$NON-NLS-1$


	public Object execute(ExecutionEvent event) throws ExecutionException {
		run();
		return null;
	}


	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		IConsole activeConsole = GrepConsoleFactory.getActiveConsole();

		Assert.isTrue(activeConsole instanceof GrepConsole);
		GrepConsole grepConsole = (GrepConsole)activeConsole;
		
		ECGModel ecgModel = ECGContext.getECGMap().get(grepConsole);
		Assert.isNotNull(ecgModel);
		
		
		if (ecgModel.isSourceDisposed()) {
			boolean closeConsoleGrep = MessageDialog.openQuestion(shell, Activator.GREP_CONSOLE_NAME, 
					Messages.RegrepHandler_CloseDisposedSourceConsoleQuestion);
			if (closeConsoleGrep) {
				ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{grepConsole});
				ECGContext.getECGMap().remove(grepConsole);
				return;
			}
		}
		
		// Flush source document 
		String sourceText = ecgModel.getSource().getDocument().get();
		grepConsole.documentChanged(new DocumentEvent(
				ecgModel.getSource().getDocument(), 0, sourceText.length(), sourceText));

	}
	
	

}
