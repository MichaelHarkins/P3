import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;
import edu.cwru.sepia.util.DistanceMetrics;
import edu.cwru.sepia.agent.Agent;

public class SearchAgent extends Agent {
	private static final long serialVersionUID = 1L;

	private Unit.UnitView footmanView, townhallView;
	private LinkedList<Node> path = new LinkedList<Node>();

	/**
	 * Constructs a search agent which will use A* which uses the equation f(n)
	 * = g(n) + h(n) to find the most optimal path. when run this agent will
	 * take a footman and try to find a path to the townhall and then destroy it
	 * 
	 * @param playernum
	 * @param otherargs
	 */
	public SearchAgent(int playernum, String[] otherargs) {
		super(playernum);
	}

	/**
	 * Takes a node and returns the path to get to that node by going to the
	 * parent of each node until the parent becomes null. It returns a list of
	 * coordinates to move to, to get to the Node.
	 * 
	 * @param n
	 *            the node to trace the route to
	 * @return A linked list of coordinates from a starting state to Node n
	 */
	public LinkedList<Node> backTrace(Node n) {
		LinkedList<Node> backtrace = new LinkedList<Node>();
		while (n.getParent() != null) {
			backtrace.addFirst(n);
			n = n.getParent();
		}
		return backtrace;
	}

	/**
	 * Checks to see if the node can move to the east, it does this by checking
	 * to see if the coordinates to move are on the map, if there are no units
	 * on that space, and that there are also no resources on that space.
	 * 
	 * @param node
	 *            The node to check if it can move to the east
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move east or false if it cannot
	 */
	private boolean canMoveEast(Node node, StateView state) {
		return (state.inBounds(node.getX() + 1, node.getY())
				&& !state.isUnitAt(node.getX() + 1, node.getY()) && !state
					.isResourceAt(node.getX() + 1, node.getY()));
	}

	/**
	 * Checks to see if the node can move to the north, it does this by checking
	 * to see if the coordinates to move are on the map, if there are no units
	 * on that space, and that there are also no resources on that space.
	 * 
	 * @param node
	 *            The node to check if it can move to the north
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move north or false if it cannot
	 */
	private boolean canMoveNorth(Node node, StateView state) {
		return (state.inBounds(node.getX(), node.getY() - 1)
				&& !state.isUnitAt(node.getX(), node.getY() - 1) && !state
					.isResourceAt(node.getX(), node.getY() - 1));
	}

	/**
	 * Checks to see if the node can move to the northeast, it does this by
	 * checking to see if the coordinates to move are on the map, if there are
	 * no units on that space, and that there are also no resources on that
	 * space.
	 * 
	 * @param node
	 *            The node to check if it can move to the northeast
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move northeast or false if it cannot
	 */
	private boolean canMoveNortheast(Node node, StateView state) {
		return (state.inBounds(node.getX() + 1, node.getY() - 1) && !state
				.isUnitAt(node.getX() + 1, node.getY() - 1))
				&& !state.isResourceAt(node.getX() + 1, node.getY() - 1);
	}

	/**
	 * Checks to see if the node can move to the northwest, it does this by
	 * checking to see if the coordinates to move are on the map, if there are
	 * no units on that space, and that there are also no resources on that
	 * space.
	 * 
	 * @param node
	 *            The node to check if it can move to the northwest
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move northwest or false if it cannot
	 */
	private boolean canMoveNorthwest(Node node, StateView state) {
		return (state.inBounds(node.getX() - 1, node.getY() - 1)
				&& !state.isUnitAt(node.getX() - 1, node.getY() - 1) && !state
					.isResourceAt(node.getX() - 1, node.getY() - 1));
	}

	/**
	 * Checks to see if the node can move to the south, it does this by checking
	 * to see if the coordinates to move are on the map, if there are no units
	 * on that space, and that there are also no resources on that space.
	 * 
	 * @param node
	 *            The node to check if it can move to the south
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move south or false if it cannot
	 */
	private boolean canMoveSouth(Node node, StateView state) {
		return (state.inBounds(node.getX(), node.getY() + 1)
				&& !state.isUnitAt(node.getX(), node.getY() + 1) && !state
					.isResourceAt(node.getX(), node.getY() + 1));
	}

	/**
	 * Checks to see if the node can move to the southeast, it does this by
	 * checking to see if the coordinates to move are on the map, if there are
	 * no units on that space, and that there are also no resources on that
	 * space.
	 * 
	 * @param node
	 *            The node to check if it can move to the southeast
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move southeast or false if it cannot
	 */
	private boolean canMoveSoutheast(Node node, StateView state) {
		return (state.inBounds(node.getX() + 1, node.getY() + 1)
				&& !state.isUnitAt(node.getX() + 1, node.getY() + 1) && !state
					.isResourceAt(node.getX() + 1, node.getY() + 1));
	}

	/**
	 * Checks to see if the node can move to the southwest, it does this by
	 * checking to see if the coordinates to move are on the map, if there are
	 * no units on that space, and that there are also no resources on that
	 * space.
	 * 
	 * @param node
	 *            The node to check if it can move to the southwest
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move southwest or false if it cannot
	 */
	private boolean canMoveSouthwest(Node node, StateView state) {
		return (state.inBounds(node.getX() - 1, node.getY() + 1)
				&& !state.isUnitAt(node.getX() - 1, node.getY() + 1) && !state
					.isResourceAt(node.getX() - 1, node.getY() + 1));
	}

	/**
	 * Checks to see if the node can move to the west, it does this by checking
	 * to see if the coordinates to move are on the map, if there are no units
	 * on that space, and that there are also no resources on that space.
	 * 
	 * @param node
	 *            The node to check if it can move to the west
	 * @param state
	 *            the current state of the map
	 * @return true if the unit can move west or false if it cannot
	 */
	private boolean canMoveWest(Node node, StateView state) {
		return (state.inBounds(node.getX() - 1, node.getY())
				&& !state.isUnitAt(node.getX() - 1, node.getY()) && !state
					.isResourceAt(node.getX() - 1, node.getY()));
	}

	/**
	 * This converts the coordinates of the position of the footman and the
	 * position it wants to move to, to a Direction please look at the Direction
	 * class for more information
	 * 
	 * @param n
	 *            the Node to move to
	 * @return the direction the Node is reletive to the footman
	 */
	private Direction convertToDirection(Node n) {
		int x = n.getX();
		int y = n.getY();
		int x2 = footmanView.getXPosition();
		int y2 = footmanView.getYPosition();
		int xDiff = x - x2;
		int yDiff = y - y2;
		switch (xDiff) {
		case 1:
			switch (yDiff) {
			case 0:
				return Direction.EAST;
			case 1:
				return Direction.SOUTHEAST;
			case -1:
				return Direction.NORTHEAST;
			}
		case -1:
			switch (yDiff) {
			case 0:
				return Direction.WEST;
			case -1:
				return Direction.NORTHWEST;
			case 1:
				return Direction.SOUTHWEST;
			}
		case 0:
			switch (yDiff) {
			case 1:
				return Direction.SOUTH;
			case -1:
				return Direction.NORTH;
			}
		}

		return null;
	}

	/**
	 * Gets the unit views of the footman and the townhall to calculate the path
	 * and the direction to move
	 * 
	 * @param state
	 *            the current state of the map
	 */
	private void getUnits(StateView state) {
		for (Integer id : state.getAllUnitIds()) {
			UnitView u = state.getUnit(id);
			if (u.getTemplateView().getName().equalsIgnoreCase("townhall"))
				townhallView = u;
			else if (u.getTemplateView().getName().equalsIgnoreCase("footman"))
				footmanView = u;
		}
	}

	/**
	 * Checks to see if the node is within 1 space of the goal
	 * 
	 * @param node
	 *            the node to see if it is by the goal
	 * @return true if the node is within one space of the goal, else it returns
	 *         false
	 */
	private boolean goal(Node node) {
		int xDiff = Math.abs(townhallView.getXPosition() - node.getX());
		int yDiff = Math.abs(townhallView.getYPosition() - node.getY());
		if (xDiff == 0) {
			return yDiff == 1;
		}
		if (yDiff == 0)
			return xDiff == 1;
		return xDiff == 1 && yDiff == 1;
	}

	@Override
	/**
	 * The initial step creates the path for the footman to follow to the goal
	 */
	public Map<Integer, Action> initialStep(StateView newstate,
			History.HistoryView statehistory) {

		getUnits(newstate);
		path = path(newstate, footmanView);
		Map<Integer, Action> myAction = new HashMap<Integer, Action>();
		return myAction;
	}

	@Override
	public void loadPlayerData(InputStream is) {
		// this agent lacks learning and so has nothing to persist.
	}

	@Override
	/**
	 * The middle state gets the positions of the townhall and the footman, and then either moves closer to the
	 * townhall or starts attacking it
	 */
	public Map<Integer, Action> middleStep(StateView newstate,
			History.HistoryView statehistory) {
		getUnits(newstate);
		return moveAndAttack(newstate);
	}

	/**
	 * This uses the path found and traverses it to reach the townhall and then
	 * it attacks the townhall and destroys it
	 * 
	 * @param currentstate
	 *            the current state of the map
	 * @return The action mapping the footman to either move to a space or to
	 *         attack the townhall
	 */
	private Map<Integer, Action> moveAndAttack(StateView currentstate) {
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		if (path == null) {
			System.out.println("No available path");
			System.exit(0);
		}
		if (!path.isEmpty()) {
			Node toMove = path.pop();
			actions.put(footmanView.getID(), Action.createPrimitiveMove(
					footmanView.getID(), convertToDirection(toMove)));
		} else {
			actions.put(footmanView.getID(), Action.createPrimitiveAttack(
					footmanView.getID(), townhallView.getID()));
		}
		return actions;
	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveEast(Node node) {
		return new Node(node.getX() + 1, node.getY(), node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX() + 1, node.getY(),
						townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveNorth(Node node) {
		return new Node(node.getX(), node.getY() - 1, node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX(), node.getY() - 1,
						townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveNortheast(Node node) {
		return new Node(node.getX() + 1, node.getY() - 1, node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX() + 1,
						node.getY() - 1, townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveNorthwest(Node node) {
		return new Node(node.getX() - 1, node.getY() - 1, node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX() - 1,
						node.getY() - 1, townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveSouth(Node node) {
		return new Node(node.getX(), node.getY() + 1, node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX(), node.getY() + 1,
						townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveSoutheast(Node node) {
		return new Node(node.getX() + 1, node.getY() + 1, node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX() + 1,
						node.getY() + 1, townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveSouthwest(Node node) {
		return new Node(node.getX() - 1, node.getY() + 1, node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX() - 1,
						node.getY() + 1, townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This creates a node in the direction, it uses the total cost of the
	 * current node to make that the new nodes g cost and then uses the
	 * chebyshev distance for the new nodes h cost.
	 * 
	 * @param node
	 *            the parent of the new node
	 * @return the new node created
	 */
	private Node moveWest(Node node) {
		return new Node(node.getX() - 1, node.getY(), node.getfCost(),
				DistanceMetrics.chebyshevDistance(node.getX() - 1, node.getY(),
						townhallView.getXPosition(),
						townhallView.getYPosition()), node);

	}

	/**
	 * This checks a node with a priortyqueue to see that when there are two
	 * nodes that are in the same position that the f cost of the node is less
	 * than the cost of the node in the open list. if that cost is less than we
	 * want to add that node to the open list because it might be a more optimal
	 * path
	 * 
	 * @param neighbor
	 *            the node to check with every node in the open list
	 * @param openList
	 *            the current list of open nodes
	 * @return true if the node is more optimal or false if it is not
	 */
	private boolean checkOpenList(Node neighbor, PriorityQueue<Node> openList) {
		for (Node check : openList) {
			if (check.getX() == neighbor.getX()
					&& check.getY() == neighbor.getY()
					&& neighbor.getfCost() > check.getfCost())
				return true;
		}
		return false;
	}

	/**
	 * This checks a node with a list to see that when there are two nodes that
	 * are in the same position that the f cost of the node is less than the
	 * cost of the node in the closed list. if that cost is less than we want to
	 * add that node to the open list because it might be a more optimal path
	 * 
	 * @param neighbor
	 *            the node to check with every node in the closed list
	 * @param closedList
	 *            the current list of closed nodes
	 * @return true if the node is more optimal or false if it is not
	 */
	private boolean checkClosedList(Node neighbor, List<Node> closedList) {
		for (Node check : closedList) {
			if (check.getX() == neighbor.getX()
					&& check.getY() == neighbor.getY()
					&& neighbor.getfCost() > check.getfCost())
				return true;
		}
		return false;
	}

	/**
	 * Computes the path using the A* algorithm f(n) = g(n) + h(n) where g(n) is
	 * the total cost up to that point and h(n) is the cost to the path to a
	 * neighboring node. It creates a open list and close list of nodes. The
	 * starting node is opened and its neighbors are checked It sees if that
	 * neighbor is the goal and if it is, it returns the path to that node. If
	 * it is not the goal it checks to see if it has already been traversed or
	 * its path is not optimal. If the path is optimal it adds that node to the
	 * open list. It keeps checking nodes until it eventually finds a path to
	 * the goal or it finds that there is no path to the goal and returns null.
	 * 
	 * @param state
	 *            the state of the map
	 * @param start
	 *            the unit view of the starting unit
	 * @return
	 */
	private LinkedList<Node> path(StateView state, Unit.UnitView start) {
		NodeComparator compare = new NodeComparator();
		PriorityQueue<Node> openList = new PriorityQueue<Node>(10, compare);
		List<Node> closedList = new LinkedList<Node>();
		Node startNode = new Node(start.getXPosition(), start.getYPosition(),
				0, 0, null);
		openList.add(startNode);
		while (!openList.isEmpty()) {
			Node temp = openList.poll();
			setNeighbors(temp, state);
			for (Node neighbor : temp.getNeighbors()) {
				boolean skip = false;
				if (goal(neighbor)) {
					return backTrace(neighbor);
				}
				skip = checkClosedList(neighbor, closedList)
						|| checkOpenList(neighbor, openList);
				if (!skip)
					openList.add(neighbor);
			}
			closedList.add(temp);
		}

		return null;

	}

	@Override
	public void savePlayerData(OutputStream os) {

	}

	/**
	 * This function checks to see what neighbors the current node can have and
	 * creates those neighbors, adding them to that nodes list of neighbors
	 * 
	 * @param node
	 *            the node to check neighbors for
	 * @param state
	 *            the current state of the map
	 */
	private void setNeighbors(Node node, StateView state) {
		List<Node> neighbors = new LinkedList<Node>();
		if (canMoveNorth(node, state)) {
			Node north = moveNorth(node);
			neighbors.add(north);
		}
		if (canMoveNortheast(node, state)) {
			Node northeast = moveNortheast(node);
			neighbors.add(northeast);
		}
		if (canMoveEast(node, state)) {
			Node east = moveEast(node);
			neighbors.add(east);
		}
		if (canMoveSoutheast(node, state)) {
			Node southeast = moveSoutheast(node);
			neighbors.add(southeast);
		}
		if (canMoveSouth(node, state)) {
			Node south = moveSouth(node);
			neighbors.add(south);
		}
		if (canMoveSouthwest(node, state)) {
			Node southwest = moveSouthwest(node);
			neighbors.add(southwest);
		}
		if (canMoveWest(node, state)) {
			Node west = moveWest(node);
			neighbors.add(west);
		}
		if (canMoveNorthwest(node, state)) {
			Node northwest = moveNorthwest(node);
			neighbors.add(northwest);
		}
		node.setNeighbors(neighbors);
	}

	@Override
	public void terminalStep(StateView newstate,
			History.HistoryView statehistory) {

	}

	public static String getUsage() {

		return "";
	}

}
