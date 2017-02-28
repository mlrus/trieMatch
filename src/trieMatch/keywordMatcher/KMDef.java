//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;

import trieMatch.Interfaces.MatchText;

/** 
 * Tight, class-based match types, setting the data structure to set or
 * list as appropriate, and making the type available through getClass.
 * @author Michah.Lerner
 *
 */
public class KMDef {
	static String splitPatt = "[^A-Za-z0-9]+";
	public static MatchText mkKdesc(String text, String matchType) {
		return KMDef.KMT.valueOf(matchType.toUpperCase(Locale.getDefault())).MT(text);
	}
	public enum KMT {
		/** Enum element for a keyword match, with the associated function to represent it internally */
		KEYWORDMATCH {
			@Override
			public MatchText MT(String s) {
				return new KeywordMatch(s);
			}
		},
		/** Enum element for an in order subsequence (skips allowed) match, with the associated function to represent it internally */
		INORDERMATCH {
			@Override
			public MatchText MT(String s) {
				return new InorderMatch(s);
			}
		},	
		/** Enum element for an in order sequence (skips not allowed) match, with the associated function to represent it internally */
		PHRASEMATCH {
			@Override
			public MatchText MT(String s) {
				return new PhraseMatch(s);
			}
		},
		/** Enum element for an exact match (all tokens of input and matched item) */
		EXACTMATCH {
			@Override
			public MatchText MT(String s) {
				return new ExactMatch(s);
			}
		};
		public abstract MatchText MT(String s);
	}
}


@SuppressWarnings("serial")
class KeywordMatch extends HashSet<String> implements MatchText {
	/**
	 * Generate a stored KeywordMatch, using a hash set to store terms
	 * @param s  the content to be stored as a keyword match.
	 */
	KeywordMatch(String s) {
		this.addAll(Arrays.asList(s.split(KMDef.splitPatt)));
	}

	public Collection get() {
		return this;
	}
}

@SuppressWarnings("serial")
class InorderMatch extends LinkedHashSet<String> implements MatchText {
	/**
	 * Generate a stored InorderMatch, using a hash set to store terms
	 * @param s the content to be stored as a keyword match.
	 */
	InorderMatch(String s) {
		this.addAll(Arrays.asList(s.split(KMDef.splitPatt)));
	}

	public Collection get() {
		return this;
	}
}

@SuppressWarnings("serial")
class PhraseMatch extends ArrayList<String> implements MatchText {
	/**
	 * Generate a stored PhraseMatch or stored inorderMatch, using a list 
	 * to store the terms.  The semantics of an inOrderMatch are handled
	 * by the matcher code, for example Michah Lerner's "km" dynamic matcher.
	 * @param s the content to be stored as a PhraseMatch.
	 */
	PhraseMatch(String s) {
		this.addAll(Arrays.asList(s.split(KMDef.splitPatt)));
	}

	public Collection get() {
		return this;
	}
}

@SuppressWarnings("serial")
class ExactMatch extends ArrayList<String> implements MatchText {
	/**
	 * Generate a stored ExactMatch, using a list to store terms
	 * @param s the content to be stored as an ExactMatch.
	 */
	ExactMatch(String s) {
		this.add(s);
	}

	public Collection get() {
		return this;
	}
}
