package de.jepfa.easyconsolegrepper.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.console.IConsole;

import de.jepfa.easyconsolegrepper.GrepConsole;
import de.jepfa.easyconsolegrepper.GrepConsoleFactory;
import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.model.ECGModel;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * Action for disabling / enabling the filter
 *
 * @author Jens Pfahl
 */
public class SwitchFilterOnOffAction extends Action {

    public SwitchFilterOnOffAction(IConsole console) {
    	super(Messages.SwitchFilterOnOffAction_FilterOnOff, Action.AS_CHECK_BOX);
    	setImageDescriptor( Activator.getImageDescriptor(Activator.IMAGE_FILTER_ON_OFF_16));
        setToolTipText(Messages.SwitchFilterOnOffAction_ToolTip_FilterOn);
    }

    public void run() {
    	IConsole activeConsole = GrepConsoleFactory.getActiveConsole();

		if (activeConsole instanceof GrepConsole) {
			GrepConsole grepConsole = (GrepConsole)activeConsole;
			ECGModel ecgModel = ECGContext.getECGMap().get(grepConsole);

			Assert.isNotNull(ecgModel);
			boolean newValue = !ecgModel.isFilterEnabled();
			if (newValue) {
				 setToolTipText(Messages.SwitchFilterOnOffAction_ToolTip_FilterOn);
			}
			else {
				 setToolTipText(Messages.SwitchFilterOnOffAction_ToolTip_FilterOff);
			}
			ecgModel.setFilterEnabled(newValue);
		}
    }
}
