package de.jepfa.easyconsolegrepper.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IConsole;

import de.jepfa.easyconsolegrepper.handler.RegrepHandler;
import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * Action for starting complete re-grepping of current input console with the current filter settings.
 *  
 * @author Jens Pfahl
 */
public class RegrepSourceInputAction extends Action {
    
    public RegrepSourceInputAction(IConsole console) {
    	super(Messages.RegrepSourceInputAction_RegrepCompleteSource, Activator.getImageDescriptor(Activator.IMAGE_REGREPP_16));
        setToolTipText(Messages.RegrepSourceInputAction_RegrepCompleteSourceTooltip); 
    }

    public void run() {
		new RegrepHandler().run();
    }
}
