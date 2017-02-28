//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.coll;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import trieMatch.util.coll.Pair;


/**
 * Two level map with iterator, allows null entries but does not honor the full iterator contract. 
 */
public class L2Map<S,T> {
	Map<S,Collection<T>>map;
	public L2Map() {
		map = new TreeMap<S,Collection<T>>();
	}
	public boolean add(S s, T t) {
		boolean newlist=false;
		Collection<T> l = map.get(s);
		if(l==null) { 
			//l=new ArrayList<T>();
			l=new  TreeSet<T>();
			map.put(s,l);
			newlist=true;
		}
		l.add(t);
		return newlist;
	}
	public Collection<T>get(S s) {
		return map.get(s);
	}
	boolean containsKey(S s) {
		return map.containsKey(s);
	}
	public boolean containsItem(S s, T t) {
		Collection<T> l = map.get(s);
		if(l==null) return false;     // a null list may not even include a null item.
		return l.contains(t);
	}
	public int getSize() { return map.size(); }
	public int getFullSize() {
		int fullSize=0;
		L2Iterator<S,T> l2i = new L2Iterator<S,T>();
		while(l2i.hasMore()) {
			//Pair<S,T> p = 
				l2i.nextPair();
			fullSize++;
			}
		return fullSize;
	}
	
	public class L2Iterator<S2 extends S, T2 extends T> {
		private Iterator<T> innerIterator=null;
		private Entry<S, Collection<T>> outerItem=null;
		private Iterator<Entry<S, Collection<T>>> outerIterator=null;
		public L2Iterator() {
			outerIterator = map.entrySet().iterator();
		}
		T nextValue() {
			return hasMore()?innerIterator.next():null;
		}
		public Pair<S,T> nextPair() {
			if(hasMore()) {
				T innerItem = innerIterator.next();
				return new Pair<S,T>(outerItem.getKey(),innerItem);
			}
			return null;
		}
		public boolean hasMore() {
			if(innerIterator!=null&&innerIterator.hasNext())return true;
			if(outerItem==null) {
				outerItem = outerIterator.next();
				if(outerItem.getValue()!=null)innerIterator=outerItem.getValue().iterator();				
			}
			if(innerIterator.hasNext())return true;
			if(!outerIterator.hasNext())return false;
			outerItem=outerIterator.next();
			innerIterator=outerItem.getValue().iterator();
			return true;
		}
	}
}


