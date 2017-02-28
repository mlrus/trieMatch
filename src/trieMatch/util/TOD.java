/**
 * 
 */
package trieMatch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michah.Lerner
 *
 */
public class TOD {
	public static SimpleDateFormat dateTime = 
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
	public static String now() {
		return dateTime.format(new Date());
	}
}
