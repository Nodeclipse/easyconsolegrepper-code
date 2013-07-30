package de.jepfa.easyconsolegrepper.converter;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.ui.console.TextConsole;

/**
 * Return the String representation of given {@link TextConsole}.
 * 
 * @author Jens Pfahl
 */
public class TextConsole2NameConverter implements IConverter {

	public Object getFromType() {
		return TextConsole.class;
	}

	public Object getToType() {
		return String.class;
	}

	public Object convert(Object fromObject) {
		TextConsole textConsole = (TextConsole)fromObject;
		if (textConsole == null) {
			return null;
		}
		return textConsole.getName();

	}

}
