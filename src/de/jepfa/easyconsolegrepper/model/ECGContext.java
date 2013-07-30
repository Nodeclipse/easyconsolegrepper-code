package de.jepfa.easyconsolegrepper.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import de.jepfa.easyconsolegrepper.GrepConsole;

/**
 * This class holds all used Grep Consoles and the Substitution String history.
 * 
 * @author Jens Pfahl
 */
public class ECGContext {

	private static Map<GrepConsole, ECGModel> ecgMap = new ConcurrentHashMap<GrepConsole, ECGModel>();
	
	private static Set<String> searchStringHistory = Collections.synchronizedSet(new TreeSet<String>());

	public static Map<GrepConsole, ECGModel> getECGMap() {
		return ecgMap;
	}
	
	public static Set<String> getSearchStringHistory() {
		return searchStringHistory;
	}

}
