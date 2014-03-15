package de.jepfa.easyconsolegrepper;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.ConsoleView;

import de.jepfa.easyconsolegrepper.handler.ConsoleConfigHandler;
import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.model.ECGModel;


/**
 * Class to create a new {@link GrepConsole}.
 * <p>
 * Parts are copied from  http://code.google.com/a/eclipselabs.org/p/console-grep
 *
 * @author Jens Pfahl
 */
@SuppressWarnings("restriction")
public class GrepConsoleFactory implements IConsoleFactory {

	@Override
	public void openConsole() {
		new ConsoleConfigHandler(true).run();
	}

	public static void createNewGrepConsole(ECGModel ecgModel) {
		// Create a new grep console
		GrepConsole grepConsole = new GrepConsole(ecgModel);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ grepConsole });

		// Update model
		ECGContext.getECGMap().put(grepConsole, ecgModel);

		// Display the created console
		IConsoleView consoleView = null;
		try {
			consoleView = (IConsoleView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IConsoleConstants.ID_CONSOLE_VIEW);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		if (consoleView != null) {
			consoleView.display(grepConsole);
		}
	}

	public static IConsole getActiveConsole() {
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (activePart instanceof ConsoleView) {
			IConsole activeConsole = ((ConsoleView)activePart).getConsole();
			return activeConsole;
		}
		return null;

	}
}
