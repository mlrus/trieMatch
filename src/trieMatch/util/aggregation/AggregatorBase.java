//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;

import trieMatch.Interfaces.Aggregator;
import trieMatch.util.Constants;

/**
 * Aggregation
 * @author Michah.Lerner
 *
 */
public abstract class AggregatorBase implements Aggregator {
	int increment=Constants.AGGREGATION_INCR;
	public AggregatorBase() { // intentionally blank;
	}
	public AggregatorBase(String increment) {
		this(Integer.parseInt(increment));
	}
	public AggregatorBase(Integer arg) {
		this();
	}
	static public Aggregator getAggregator(String aggregatorName, String aggregatorParm) {
		Aggregator aggregator=Constants.DEFAULT_AGGREGATOR;
		try {
			Class aggregatorClass = Class.forName(Constants.DEFAULT_AGGREGATOR_BASE+"."+aggregatorName);
			aggregator = (Aggregator) (aggregatorParm == null 
					? aggregatorClass.getConstructor().newInstance() 
					: aggregatorClass.getConstructor(String.class).newInstance(aggregatorParm));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aggregator;
	}
}
