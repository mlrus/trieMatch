//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import trieMatch.Interfaces.Aggregator;
import trieMatch.util.Constants;

	/**
	 * Name equivalent implementation of business logic for online aggregation and ranking of results.
	 * @author Michah.Lerner
	 */

public class KMTally {

		Map<String,KMDefinition> smap;
		SortedMap<KMDefinition,KMDefinition> valmap;
		static int _maxElements = -1; // If normal, 2X would be enough
		int maxElements = -1;
		static Aggregator aggregator=Constants.DEFAULT_AGGREGATOR; 
		KMTally() {
			this(_maxElements);
		}
		KMTally(int maxElements) {
			this.maxElements = maxElements;
			smap = new HashMap<String,KMDefinition>();
			valmap=new TreeMap<KMDefinition,KMDefinition>(new CompareEntries());
		}

		void addItem(KMDefinition km, int tally) {
			KMDefinition kmd = smap.get(km.url);
			if (kmd == null) {
				kmd = new KMDefinition(km);
				smap.put(km.url, kmd);
			} else {
				valmap.remove(kmd);
			}
			kmd.score=aggregator.aggregate(kmd.score,tally);
			valmap.put(kmd,kmd);
			truncate();
		}

		void add(KMDefinition km) {
			addItem(km,km.score);
		}
		
		/**
		 * Add all the raw scores into full scores 
		 * @param km
		 */
		@SuppressWarnings("unchecked")
		void addAll(Collection<KMDefinition>km) {
			List<KMDefinition> kmdList = null;
			if(km instanceof ArrayList) {
				kmdList = (ArrayList<KMDefinition>)((ArrayList<KMDefinition>)km).clone();
			} else {
				kmdList = new ArrayList<KMDefinition>(km);
			}
			for(KMDefinition kmd : km) add(kmd);
			Collections.sort(kmdList, valmap.comparator());
			Iterator<KMDefinition> kmi = kmdList.iterator();
			if(!kmi.hasNext())return;
			KMDefinition prior = kmi.next(); 
			while(kmi.hasNext()) {
				KMDefinition next = kmi.next();
				if(prior.url.equalsIgnoreCase(next.url)) {
					prior.score = aggregator.aggregate(prior.score,next.score);
					continue;
				}
				add(prior);
				prior=next;
			}
			add(prior);
		}
		
		void truncate() {
			if(maxElements<0)return;
			while(valmap.size()>maxElements) {
				KMDefinition dropItem = valmap.firstKey();
				valmap.remove(dropItem);
				smap.remove(dropItem.url);
			}
		}
		
		void showItems() {
			for(KMDefinition kmd : valmap.keySet()) {
				System.out.println(kmd.asString());
			}
		}
		
		class CompareEntries implements Comparator<KMDefinition> {
			public int compare(KMDefinition o1, KMDefinition o2) {
				int c = o1.score-o2.score;
				if(c!=0)return c;
				return(-o1.url.compareTo(o2.url));
			}
		}	
}
