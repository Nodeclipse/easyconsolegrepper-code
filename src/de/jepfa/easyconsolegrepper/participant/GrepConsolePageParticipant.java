package de.jepfa.easyconsolegrepper.participant;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;

import de.jepfa.easyconsolegrepper.GrepConsole;
import de.jepfa.easyconsolegrepper.GrepConsoleStyleListener;
import de.jepfa.easyconsolegrepper.actions.ChangeSettingsAction;
import de.jepfa.easyconsolegrepper.actions.RegrepSourceInputAction;
import de.jepfa.easyconsolegrepper.actions.ViewGrepPrefsAction;
import de.jepfa.easyconsolegrepper.model.ECGContext;

/**
 * This class is responsible for creating Grep Console tool buttons and the life
 * cycle behavoir of the Grep Console page.
 * 
 * @author Jens Pfahl
 */
public class GrepConsolePageParticipant implements IConsolePageParticipant {

	private CloseConsoleAction closeAction;
	private ChangeSettingsAction changeSettingsAction;
	private RegrepSourceInputAction regrepSourceInputAction;
	private GrepConsole grepConsole;
	private GrepConsoleStyleListener styleListener;
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IBasicPropertyConstants.P_TEXT )) {
				grepConsole.refresh();
			}
		}
	};
	private StyledText viewer;
	private ViewGrepPrefsAction viewGrepPrefsAction;

	public void init(IPageBookViewPage page, IConsole console) {
		this.grepConsole = (GrepConsole) console;

		// create Close tool button
		closeAction = new CloseConsoleAction(console);
		IToolBarManager manager = page.getSite().getActionBars()
				.getToolBarManager();
		manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, closeAction);

		// create Grep Console tool button
		changeSettingsAction = new ChangeSettingsAction(console);
		manager.appendToGroup(IConsoleConstants.OUTPUT_GROUP,
				changeSettingsAction);
		// create Regrep tool button
		regrepSourceInputAction = new RegrepSourceInputAction(console);
		manager.appendToGroup(IConsoleConstants.OUTPUT_GROUP,
				regrepSourceInputAction);
		// create Prefs tool button
		viewGrepPrefsAction = new ViewGrepPrefsAction();
		manager.appendToGroup(IConsoleConstants.OUTPUT_GROUP,
				viewGrepPrefsAction);
		
		if ((page.getControl() instanceof StyledText)) {
	      viewer = (StyledText)page.getControl();
	      styleListener = new GrepConsoleStyleListener(grepConsole, viewer);
	      viewer.addLineStyleListener(styleListener);
	    }
		
		
		grepConsole.getModel().getSource().addPropertyChangeListener(propertyChangeListener);
	}

	public void dispose() {
		grepConsole.getModel().getSource().removePropertyChangeListener(propertyChangeListener);
		closeAction = null;
		changeSettingsAction= null;
		ECGContext.getECGMap().remove(grepConsole);
		if (viewer != null && !viewer.isDisposed()) {
			viewer.removeLineStyleListener(styleListener);
		}
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public void activated() {
	}

	public void deactivated() {
	}

}
