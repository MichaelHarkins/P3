import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {

	@Override
	/**
	 * Compares two nodes 
	 */
	public int compare(Node node1, Node node2) {
		if (node1.getfCost() < node2.getfCost())
			return -1;
		if (node1.getfCost() > node2.getfCost())
			return 1;
		return 0;

	}

}
