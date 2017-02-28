//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.coll;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Typesafe pairs which implements a comparator over pairs. Also works
 * when the component types are collections of comparable objects, using
 * the natural iteration order of the collections.
 * @author Michah.Lerner
 *
 * @param <S> type "car" "head" or "s" of the pair
 * @param <T> type "cdr" "tail" or "t" of the pair
 */
public class Pair<S, T>  implements Comparator<Pair<S,T>>{
	S s;
	T t;
	
	@SuppressWarnings("unchecked")
	public Pair() {
		s=(S)("");
		t=(T)(Integer.valueOf(-1));
	}
	
	public Pair(S s, T t) {
		this.s = s;
		this.t = t;
	}

	public S S() {
		return s;
	}

	public T T() {
		return t;
	}
	public Pair<S,T> set(S s,T t) { this.s=s; this.t=t; return this; }
	public S setS(S s) { this.s=s; return s; }
	public T setT(T t) { this.t=t; return t; }

	public S s() { return s; }
	public T t() { return t; }
	@Override
	public String toString() { return "{" + s().toString()+", "+t().toString()+"}"; }
	
	@SuppressWarnings("unchecked")
	int cmp(Object o1, Object o2) {
		if(o1 instanceof Comparable && o2 instanceof Comparable) {
			return ((Comparable)o1).compareTo(o2);
		}
		if(o1 instanceof Iterable && o2 instanceof Iterable) {
			Iterator i1 = ((Iterable) o1).iterator();
			Iterator i2 = ((Iterable) o2).iterator();
			if(i1.hasNext()&&i2.hasNext()) {
				int cmp = ((Comparable) i1.next()).compareTo(i2.next());
				if(cmp!=0)return cmp;
			}
			if(i1.hasNext())return -1;
			if(i2.hasNext())return 1;
		}
		return 0;	
	}


	public int compare(Pair<S, T> o1, Pair<S,T>o2) {
		int cmp=cmp(o1.S(),o2.S());
		if(cmp==0)cmp=cmp(o1.S(),o2.T());
		return cmp;
	}

	public int compare(Pair o2) {
		return cmp(this,o2);
	}


}
