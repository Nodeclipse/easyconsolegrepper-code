package de.jepfa.easyconsolegrepper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Hex;

import de.jepfa.easyconsolegrepper.GrepConsole;
import de.jepfa.easyconsolegrepper.internal.Activator;

/**
 * This class holds all used Grep Consoles and the Substitution String history.
 * 
 * @author Jens Pfahl
 */
public class ECGContext {
	
	private static final String SEARCH_STRING_HISTORY_KEY = "PREF_HISTORY_ENTRIES"; //$NON-NLS-1$
	
	

	private static Map<GrepConsole, ECGModel> ecgMap = new ConcurrentHashMap<GrepConsole, ECGModel>();
	
	private static Set<String> searchStringHistory = Collections.synchronizedSet(new TreeSet<String>());

	public static Map<GrepConsole, ECGModel> getECGMap() {
		return ecgMap;
	}
	
	public static Set<String> getSearchStringHistory() {
		return searchStringHistory;
	}

	public static void load() {
		String searchStringHistoryData = Activator.getDefault().getDialogSettings().get(SEARCH_STRING_HISTORY_KEY);
		if (searchStringHistoryData != null) {
			try {
				getSearchStringHistory().addAll(getStringAsList(searchStringHistoryData));
			} catch (DecoderException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void persist() {
		try {
			Activator.getDefault().getDialogSettings().put(SEARCH_STRING_HISTORY_KEY, getListAsString(getSearchStringHistory()));
		} catch (EncoderException e) {
			e.printStackTrace();
		}
	}

	private static Collection<String> getStringAsList(String string) throws DecoderException {
		List<String> returnList = new ArrayList<String>();
		String[] split = string.split(","); //$NON-NLS-1$
		Hex hex = new Hex();
		for (String encodedString : split) {
			byte[] decoded = (byte[]) hex.decode(encodedString);
			String decodedString = new String(decoded);
			returnList.add(decodedString);
		}
		return returnList;
	}

	private static String getListAsString(Collection<String> collection) throws EncoderException {
		StringBuffer sb = new StringBuffer();
		Hex hex = new Hex();
		for (String string : collection) {
			String encodedString = new String((char[])hex.encode(string));
			sb.append(encodedString).append(","); //$NON-NLS-1$
		}
		return sb.toString();
	}

}
