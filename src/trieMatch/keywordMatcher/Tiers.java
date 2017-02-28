//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store all matchtext as a sorted trie of strings.  The "Sequencer" structures 
 * the primary facet of a complex input (i.e., the "text" part of a "KMDefinition").  The
 * successive elements are stored as sequence of Tiers, chained as "successors".  Any tier
 * with a final element(s) stores the full KMDefinition (as "instances"). 
 * @see Sequencer
 * @see KMDefinition
 * @author Michah.Lerner
 */
		
public class Tiers {
		
	static final List<KMDefinition> emptyValueRecord = new ArrayList<KMDefinition>(1);
	public static Class mapClass = HashMap.class; // Matcher.getMapClass() may redefine this.
	
	boolean end;                           // any instances at this depth?
	Map<String, Tiers> successors;         // next step
	ArrayList<KMDefinition> instances;     // value(s) of the path
	Sequencer elementIndexer;
	Tiers() { this(new Sequencer()); }     // not used
	
	@SuppressWarnings("unchecked")
	Tiers(Sequencer elementIndexer) {
		end = false;
		this.elementIndexer=elementIndexer;
		try {
			successors = (Map<String, Tiers>) mapClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	Tiers(Tiers tiers) {
		end = tiers.end;
		try {
			successors =(Map<String, Tiers>) mapClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if(tiers.instances!=null) {
			instances = new ArrayList<KMDefinition>(tiers.instances.size());
			for (KMDefinition km : tiers.instances)
				instances.add(new KMDefinition(km));
		}
		for (Map.Entry<String, Tiers> me : tiers.successors.entrySet())
			successors.put(me.getKey(), new Tiers(me.getValue()));
	}

	/**
	 * Add a string to the trie structure
	 * @param s the string to add
	 */
	public void add(String s) {
		add(new KMDefinition(s,this.elementIndexer));
	}
	
	/**
	 * Add a string, using the given sequencer
	 * @param s
	 * @param elementIndexerH
	 */
	public void add(String s, Sequencer elementIndexerH) {
		add(new KMDefinition(s,elementIndexerH));
	}
	
	/**
	 * Copies all elements of kmDef.  Do not use except in non-shared memory, due to costs.
	 * @param kmDef
	 */
	public void add(KMDefinition kmDef) {
		Tiers current = this;
		Tiers prior = null;
		for (Atom atom : kmDef.element) {
			if (!current.successors.containsKey(atom.value)) {
				Tiers next = new Tiers(elementIndexer);
				current.successors.put(atom.value, next);
				prior = current;
				current = next;
			} else {
				prior = current;
				current = current.successors.get(atom.value);
			}
			prior.successors.put(atom.value, current);
		}
		current.end = true;
		if (current.instances == null) current.instances = new ArrayList<KMDefinition>();
		current.instances.add(kmDef);
	}

	public void printTiers(PrintStream out) {
		out.println(_printTiers("R.").toString());
	}

	private StringBuffer _printTiers(String prefix) {
		StringBuffer sb = new StringBuffer();
		int n=1;
		if(instances!=null&&instances.size()>0) {
			for(KMDefinition kmde : instances) {
				System.out.printf("%s%d %s\n",prefix,n++,kmde.asString());
			}
		}
		n = 1;
		if(successors!=null&&successors.size()>0)
		for(Map.Entry<String,Tiers> succ : successors.entrySet()) {
			succ.getValue()._printTiers(String.format("%s%d.",prefix,n++));
		}
		return sb;
	}
}
