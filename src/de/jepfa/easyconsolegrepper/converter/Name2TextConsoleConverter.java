package de.jepfa.easyconsolegrepper.converter;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

/**
 * Finds the corresponding {@link TextConsole} for the given String.
 * 
 * @author Jens Pfahl
 */
public class Name2TextConsoleConverter implements IConverter {

	public Object getFromType() {
		return String.class;
	}

	public Object getToType() {
		return TextConsole.class;
	}

	public Object convert(Object fromObject) {
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (IConsole iConsole : consoles) {
			if (iConsole instanceof TextConsole && iConsole.getName().equals(fromObject)) {
				return iConsole;
			}
		}
		return null;
	}

}
