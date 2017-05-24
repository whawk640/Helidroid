package heli.org.helidroid;

/** Chopper Aggregator, Copyright 2015, Dan and Sasha
 *
 * @author Daniel LaFuze
 *
 */

/** This class is just for convenience so the world doesn't have to
 * keep track of all of this stuff manually
 * @author Daniel
 *
 */
public class ChopperAggregator
{
    private StigChopper theChopper;
    private ChopperInfo chopInfo;
	private ChopperPanel chopPanel;

    // Define constants in here that are needed in both StigChopper and ChopperInfo
    public static final double BASE_MASS = 100.0; // kg

    // Total Capacity can be divided however desired between cargo and fuel
    public static final double TOTAL_CAPACITY = 300.0; // kg

    public static final double ITEM_WEIGHT = 10.0;

    public ChopperAggregator(StigChopper chop, ChopperInfo inf, ChopperPanel pan) {
        theChopper = chop;
        chopInfo = inf;
		chopPanel = pan;
    }

	public ChopperPanel getPanel() { return chopPanel; }
	
    public StigChopper getChopper() { return theChopper; }

    public ChopperInfo getInfo() { return chopInfo; }

    public void setInfo(ChopperInfo newInfo) { chopInfo = newInfo; }

    public void setChopper(StigChopper newChopper) { theChopper = newChopper; }
}
