//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Trie element storage with items arranged by the sequencer.
 * @author Michah.Lerner
 * 
 */
public class StructuredElement extends ArrayList<Atom> implements Element<Atom> {

	private static final long serialVersionUID = 1L;
	private static Map<Atom,Atom>primalAtoms=new HashMap<Atom,Atom>();
	private static Map<StructuredElement,StructuredElement>primalElements=new HashMap<StructuredElement,StructuredElement>();
	StructuredElement() {
		super();
	}

	StructuredElement newElement(Collection<String>tokens) {
		return newElement(tokens,false);
	}
	StructuredElement newElement(Collection<String>tokens, boolean reuseStorage) {
		StructuredElement element = new StructuredElement();
		int i=0;
		for(String token : tokens) element._add(token,i++, reuseStorage);
		element.arrangeAtoms();
		StructuredElement primalElement=primalElements.get(element);
		if(primalElement==null) {
			if(reuseStorage)primalElements.put(element,element);
			primalElement=element;
		}
		return primalElement;
	}

	public boolean _add(String item, int index) {
		return _add(item, index, false);
	}
	public boolean _add(String item, int index, boolean reuseStorage) {
		Atom atom = new Atom(item,index);
		Atom primalAtom = primalAtoms.get(atom);
		if(primalAtom==null){
			if(reuseStorage)primalAtoms.put(atom,atom);
			primalAtom=atom;
		}
		super.add(primalAtom);
		return atom.equals(primalAtom);
	}

	@Override
	public boolean add(Atom atom) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public String getValue(int i) {
		String s = this.get(i).value;
		return s;
	}

	public int getIndex(int i) {
		return this.get(i).index;
	}

	public void arrangeAtoms() {
		this.trimToSize();
		Collections.sort(this);
	}

	public String describe() {
		StringBuffer sb = new StringBuffer();
		for(Atom a : this)sb.append(a.index+"/"+a.value+" ");
		if(sb.length()>0)sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public String asString() {
		StringBuffer sb = new StringBuffer();
		for(Atom atom : this) sb.append(atom.value+" ");
		if(sb.length()>0)sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public Collection<String> allValues() {
		ArrayList<String> cs = new ArrayList<String>(this.size());
		for (Atom atom : this)
			cs.add(atom.value);
		return cs;
	}

	public static void main(String[]args) {
		System.out.println("START");
		StructuredElement instance = new StructuredElement();
		Set<StructuredElement>s1=new HashSet<StructuredElement>();

		StructuredElement a = instance.newElement(Arrays.asList("now is the".split("\\s+")));
		System.out.println("a="+a.hashCode()+":"+a.toString());
		StructuredElement b = instance.newElement(Arrays.asList("now is the".split("\\s+")));
		System.out.println("b="+b.hashCode()+":"+b.toString());
		s1.add(a);
		System.out.println("contains?a");System.out.println(s1.contains(a));
		System.out.println("contains?b");System.out.println(s1.contains(b));
		s1.add(b);
		System.out.println("contains?a");System.out.println(s1.contains(a));
		System.out.println("contains?b");System.out.println(s1.contains(b));
		a.add(new Atom("hi",1));
//		System.out.println("A::"+a.describe()+" a.hashCode="+a.hashCode());
//		System.out.println("B::"+b.describe()+" b.hashCode="+b.hashCode());
//		System.out.println("a.compareTo(b)==>"+a.compareTo(b));	
//		System.out.println("codes: " + a.hashCode() + " ; " + b.hashCode());
//		System.out.println(a);
//		for(Atom at : a)System.out.println("a:"+at+":"+at.hashCode());
//		for(Atom at : b)System.out.println("b:"+at+":"+at.hashCode());		

	}
}

class Atom implements Comparable<Atom> {
	String value;
	Integer index;
	Atom(String string, int index) {
		this.value = string;
		this.index = index;
	}

	@Override
	public boolean equals(Object o) {
		if(!o.getClass().equals(this.getClass()))return false;
		return(compareTo((Atom)o)==0);
	}
	@Override
	public int hashCode() {
		return value.hashCode()+index.hashCode();
	}

	public int compareTo(Atom o) {
		int res = value.compareToIgnoreCase(o.value);
		//if(res==0) res=index-o.index;
		return res;
	}
}
