//This is unpublished source code. Michah Lerner 2006

package trieMatch.util;

import java.util.regex.Pattern;


/**
 * Return a Double <em>without throwing an exception when there input is not a valid double</em> 
 * since exception handling is relatively expensive.  Isntead this routine will return NaN if the input
 * is not a number.   The base code was is from one of Sun's web pages, and is probably also available
 * through open source java.  .
 */
public class SafeStringToDouble {
		final static String Digits = "(\\p{Digit}+)";
		final  static String HexDigits = "(\\p{XDigit}+)";
		// an exponent is 'e' or 'E' followed by an optionally signed decimal integer.
		final static String Exp = "[eE][+-]?" + Digits;
		final static String fpRegex = ("[\\x00-\\x20]*" + // Optional leading "whitespace"
				"[+-]?(" + 		// Optional sign character
				"NaN|" + 		// "NaN" string
				"Infinity|" + 	// "Infinity" string
				// A decimal floating-point string representing a finite positive
				// number without a leading sign has at most five basic pieces:
				// Digits . Digits ExponentPart FloatTypeSuffix
				// 
				// Since this method allows integer-only strings as input
				// in addition to strings of floating-point literals, the
				// two sub-patterns below are simplifications of the grammar
				// productions from the Java Language Specification, 2nd 
				// edition, section 3.10.2.

				// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
				"(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +
				// . Digits ExponentPart_opt FloatTypeSuffix_opt
				"(\\.(" + Digits + ")(" + Exp + ")?)|" +
				// Hexadecimal strings
				"((" +
				// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
				"(0[xX]" + HexDigits + "(\\.)?)|" +
				// 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
				"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
				")[pP][+-]?" + Digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*");// Optional trailing "whitespace"
		final static Pattern compiledPattern = Pattern.compile(fpRegex);
		
	/**
	 * @param myString a string that is supposed to, in its entirety, represent a double 
	 * @return either the double value, or NaN if the string is not a well-formatted double.
	 */
		public static Double validDouble(String myString) {
			if(compiledPattern.matcher(myString).matches())
				return Double.valueOf(myString); // Will not throw NumberFormatException
		return Double.NaN;
	}

}
