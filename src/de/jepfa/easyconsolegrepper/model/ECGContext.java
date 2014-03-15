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

	private static final int MAX_SEARCH_STRING_HISTORY_LENGTH = 100;

	private static Map<GrepConsole, ECGModel> ecgMap = new ConcurrentHashMap<GrepConsole, ECGModel>();

	private static Set<SearchStringElem> searchStringHistory = Collections.synchronizedSet(new TreeSet<SearchStringElem>());

	public static Map<GrepConsole, ECGModel> getECGMap() {
		return ecgMap;
	}

	public static Set<SearchStringElem> getSortedSearchStringHistory() {
		return searchStringHistory;
	}

	public static void load() {
		String searchStringHistoryData = Activator.getDefault().getDialogSettings().get(SEARCH_STRING_HISTORY_KEY);
		if (searchStringHistoryData != null) {
			try {
				searchStringHistory.addAll(getStringAsList(searchStringHistoryData));
			} catch (DecoderException e) {
				e.printStackTrace();
			}
		}
	}

	public static void persist() {
		try {
			Activator.getDefault().getDialogSettings().put(SEARCH_STRING_HISTORY_KEY, getListAsString(searchStringHistory));
		} catch (EncoderException e) {
			e.printStackTrace();
		}
	}

	private static Collection<SearchStringElem> getStringAsList(String string) throws DecoderException {
		List<SearchStringElem> returnList = new ArrayList<SearchStringElem>();
		String[] split = string.split(","); //$NON-NLS-1$
		Hex hex = new Hex();
		int stamp = split.length;
		for (String s : split) {
			stamp--;
			byte[] decoded = (byte[]) hex.decode(s);
			String decodedString = new String(decoded);
			returnList.add(new SearchStringElem(stamp, decodedString));
		}
		return returnList;
	}

	private static String getListAsString(Collection<SearchStringElem> collection) throws EncoderException {
		StringBuffer sb = new StringBuffer();
		Hex hex = new Hex();
		int counter = 0;
		for (SearchStringElem elem : collection) {
			if (counter == MAX_SEARCH_STRING_HISTORY_LENGTH) {
				break;
			}
			if (elem.getSearchString().isEmpty()) {
				continue;
			}
			String encodedString = new String((char[])hex.encode(elem.getSearchString()));
			sb.append(encodedString).append(","); //$NON-NLS-1$
			counter++;
		}
		return sb.toString();
	}

}
