package de.jepfa.easyconsolegrepper.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IConsole;

import de.jepfa.easyconsolegrepper.handler.ConsoleConfigHandler;
import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * Action for providing change of current Grep Console settings.
 *  
 * @author Jens Pfahl
 */
public class ChangeSettingsAction extends Action {
    
    public ChangeSettingsAction(IConsole console) {
        super(Messages.ChangeSettingsAction_ChangeSettings, Activator.getImageDescriptor(Activator.IMAGE_GREP_CONSOLE_16));
        setToolTipText(Messages.ChangeSettingsAction_ChangeCurrentFilterSettings); 
    }

    public void run() {
		new ConsoleConfigHandler().run();
    }
}
