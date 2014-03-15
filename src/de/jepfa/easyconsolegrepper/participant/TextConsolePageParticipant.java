package de.jepfa.easyconsolegrepper.participant;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

import de.jepfa.easyconsolegrepper.GrepConsole;
import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.model.ECGModel;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * This class is responsible for updating all Grep Consoles whether an
 * observed Text Console has been disposed.
 *
 * @author Jens Pfahl
 */
public class TextConsolePageParticipant implements IConsolePageParticipant {

	private IConsole console;

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		this.console = console;

		boolean resumeTerminatedConsole = Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREF_RESUME_TERMINATED_CONSOLE);
		boolean activateConsoleOnResuming = Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREF_ACTIVATE_CONSOLE_ON_RESUMING);

		if (resumeTerminatedConsole
				&& !(console instanceof GrepConsole)) {
			for (Entry<GrepConsole, ECGModel> entry : ECGContext.getECGMap().entrySet()) {
				GrepConsole grepConsole = entry.getKey();
				ECGModel ecgModel = entry.getValue();

				if (ecgModel.isSourceDisposed()) {
					String originSourceName = prepareString(ecgModel.getSourceName());
					String newSourceName = prepareString(console.getName());

					if (originSourceName.equals(newSourceName)) {
						ecgModel.setSourceDisposed(false);
						ecgModel.setSource((TextConsole) console);
						grepConsole.updateModel(ecgModel);
						DateFormat sdf = SimpleDateFormat.getDateTimeInstance(
								SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM);
						grepConsole.writeToConsole(MessageFormat.format(
								Messages.TextConsolePageParticipant_ResumingConsoleGrepping,
								Activator.GREP_CONSOLE_OUTPUT_PREFIX, sdf.format(new Date())));
						if (activateConsoleOnResuming) {
							grepConsole.activate();
						}
					}
				}
			}
		}

	}

	@Override
	public void dispose() {
		if (!(console instanceof GrepConsole)) {
			for (Entry<GrepConsole, ECGModel> entry : ECGContext.getECGMap().entrySet()) {
				ECGModel ecgModel = entry.getValue();
				if (ecgModel.getSource() == console) {
					ecgModel.setSourceDisposed(true);
					entry.getKey().updateModel(ecgModel);
				}
			}
		}

	}

	@Override
	public void activated() {

	}

	@Override
	public void deactivated() {

	}

	private String prepareString(String s) {
		return s.replaceAll("\\d", "").replace("<terminated>", "").replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}

}
