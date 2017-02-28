//This is unpublished source code. Michah Lerner 2006

package trieMatch.Interfaces;

/**
 * Marker inferface, to be used by implementations that assert that they handle
 * MatchText. This kind of empty interface is well accepted and,
 * for example, it described by (@see
 * http://en.wikipedia.org/wiki/Marker_interface_pattern) as "The marker
 * interface pattern is a design pattern in computer science, used with
 * languages that provide run-time type information about objects. It provides a
 * means to associate metadata with a class where the language does not have
 * explicit support for such metadata."
 * 
 * @author Michah.Lerner
 * 
 */
public interface MatchText {
// marker interface 
	Object get();
}
