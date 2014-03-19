import java.util.Comparator;

public class RCStateComparator implements Comparator<RCState> {

	@Override
	/**
	 * Compares two nodes 
	 */
	public int compare(RCState node1, RCState node2) {
		if (node1.getfCost() < node2.getfCost())
			return -1;
		if (node1.getfCost() > node2.getfCost())
			return 1;
		return 0;

	}

}
