import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.experiment.Configuration;
import edu.cwru.sepia.experiment.ConfigurationValues;

/**
 * This agent will first collect gold to produce a peasant,
 * then the two peasants will collect gold and wood separately until reach goal.
 * @author Kai
 *
 */
public class RCAgent extends Agent {
	private static final long serialVersionUID = -4047208702628325380L;
	

	private int goldRequired;
	private int woodRequired;
	private List<String> peasants = new LinkedList<String>();
	private List<String> townhalls = new LinkedList<String>();
	
	private int step;
	
	public RCAgent(int playernum, String[] arguments) {
		super(playernum);
		
		goldRequired = Integer.parseInt(arguments[0]);
		woodRequired = Integer.parseInt(arguments[1]);
	}

	StateView currentState;

	

	public List<String> getInitial(){
		List<String> initial = new LinkedList<String>();
		initial.add("Peasant(p1)");
		initial.add("Idle(p1)");
		initial.add("Townhall(t1)");
		initial.add("Holding(nil,p1)");
		initial.add("Near(t1,p1)");
		initial.add("Gold(0,t1)");
		initial.add("Wood(0,t1)");
		return initial;
		
	}

	public void setNeighbors(RCState state){
		List<RCState> neighbors = new LinkedList<RCState>();
		for(String p: peasants){
			RCState s = state.clone();
			if(s.HarvestWood(p)){
				s.setAction("HarvestWood(" + p + ")");
				neighbors.add(s);
			}
			RCState s2 = state.clone();
			if(s2.HarvestGold(p)){
				s2.setAction("HarvestGold("+ p + ")");
				neighbors.add(s2);
			}
			RCState s3 = state.clone();
			if(s3.DepositWood("t1",p)){
				s3.setAction("DepositWood(" + p + ")");
				neighbors.add(s3);
			}
			RCState s4 = state.clone();
			if(s4.DepositGold("t1",p)){
				s4.setAction("DepositGold(" + p + ")");
				neighbors.add(s4);
			}
			RCState s5 = state.clone();
			if(s5.GoNear("g",p)){
				s5.setAction("GoNear(g," + p + ")");
				neighbors.add(s5);
			}
			RCState s6 = state.clone();
			if(s6.GoNear("w",p)){
				s6.setAction("GoNear(w," + p + ")");
				neighbors.add(s6);
			}
			RCState s7 = state.clone();
			if(s7.GoNear("t1",p)){
				s7.setAction("GoNear(t1," + p + ")");
				neighbors.add(s7);
			}

		}
		for(RCState s : neighbors){
			heuristic(s);
		}
		state.setNeighbors(neighbors);
	}
	public List<String> goal(int wood,int gold){
	 List<String> goal = new LinkedList<String>();
	 goal.add("Gold(" +gold + ",t1)");
	 goal.add("Wood(" + wood + ",t1)");
	 return goal;
	}

	public LinkedList<String> backTrace(RCState n) {
		LinkedList<String> backtrace = new LinkedList<String>();
		while (n.getParent() != null) {
			backtrace.addFirst(n.getAction());
			n = n.getParent();
		}
		return backtrace;
	}

	@Override
	public Map<Integer, Action> initialStep(StateView newstate, History.HistoryView statehistory) {
		step = 0;
		return middleStep(newstate, statehistory);
	}

	@Override
	public Map<Integer,Action> middleStep(StateView newState, History.HistoryView statehistory) {
		step++;

		Map<Integer,Action> builder = new HashMap<Integer,Action>();
		currentState = newState;
		
		int currentGold = currentState.getResourceAmount(0, ResourceType.GOLD);
		int currentWood = currentState.getResourceAmount(0, ResourceType.WOOD);

		List<Integer> allUnitIds = currentState.getAllUnitIds();
		List<Integer> peasantIds = new ArrayList<Integer>();
		List<Integer> townhallIds = new ArrayList<Integer>();
		for(int i=0; i<allUnitIds.size(); i++) {
			int id = allUnitIds.get(i);
			UnitView unit = currentState.getUnit(id);
			String unitTypeName = unit.getTemplateView().getName();
			if(unitTypeName.equals("TownHall"))
				townhallIds.add(id);
			if(unitTypeName.equals("Peasant"))
				peasantIds.add(id);
		}
		
		if(peasantIds.size()>=2) {  // collect resources
			if(currentWood<woodRequired) {
				int peasantId = peasantIds.get(1);
				int townhallId = townhallIds.get(0);
				Action b = null;
				if(currentState.getUnit(peasantId).getCargoAmount()>0)
					b = new TargetedAction(peasantId, ActionType.COMPOUNDDEPOSIT, townhallId);
				else {
					List<Integer> resourceIds = currentState.getResourceNodeIds(Type.TREE);
					b = new TargetedAction(peasantId, ActionType.COMPOUNDGATHER, resourceIds.get(0));
				}
				builder.put(peasantId, b);
			}
			if(currentGold<goldRequired) {
				int peasantId = peasantIds.get(0);
				int townhallId = townhallIds.get(0);
				Action b = null;
				if(currentState.getUnit(peasantId).getCargoType() == ResourceType.GOLD && currentState.getUnit(peasantId).getCargoAmount()>0)
					b = new TargetedAction(peasantId, ActionType.COMPOUNDDEPOSIT, townhallId);
				else {
					List<Integer> resourceIds = currentState.getResourceNodeIds(Type.GOLD_MINE);
					b = new TargetedAction(peasantId, ActionType.COMPOUNDGATHER, resourceIds.get(0));
				}
				builder.put(peasantId, b);
			}
		}
		else {  // build peasant
			if(currentGold>=400) {
			
				TemplateView peasanttemplate = currentState.getTemplate(playernum, "Peasant");
				int peasanttemplateID = peasanttemplate.getID();
	
				int townhallID = townhallIds.get(0);
				builder.put(townhallID, Action.createCompoundProduction(townhallID, peasanttemplateID));
			} else {
				int peasantId = peasantIds.get(0);
				int townhallId = townhallIds.get(0);
				Action b = null;
				if(currentState.getUnit(peasantId).getCargoType() == ResourceType.GOLD && currentState.getUnit(peasantId).getCargoAmount()>0)
					b = new TargetedAction(peasantId, ActionType.COMPOUNDDEPOSIT, townhallId);
				else {
					List<Integer> resourceIds = currentState.getResourceNodeIds(Type.GOLD_MINE);
					b = new TargetedAction(peasantId, ActionType.COMPOUNDGATHER, resourceIds.get(0));
				}
				builder.put(peasantId, b);
			}
		}
		return builder;
	}

	public void heuristic(RCState state)
	{
		int cost = 0;
		int currentWood =state.getWood("t1");
		int currentGold = state.getGold("t1");
		int woodDifference = woodRequired - currentWood;
		int goldDifference = goldRequired - currentGold;
		cost+= woodDifference;
		cost+=goldDifference;
		if(woodDifference <= 0 && goldDifference <= 0){
			state.sethCost(0);
			state.setgCost(state.getgCost() + 1);
			state.setfCost(state.getgCost() + state.gethCost());
			return;
		}
		if(state.isNear("p1","g") &&  state.HoldingWood("p1") )
			cost+= 10000;
		else if(state.isNear("p1","f") && state.HoldingGold("p1"))
			cost+= 10000;
		if(woodDifference == 0 && state.HoldingWood ("p1") || state.isNear("p1","f"))
			cost+= 10000;
		if(goldDifference == 0 && state.HoldingWood("p1") || state.isNear("p1","g"))
			cost+= 10000;

			state.sethCost(cost);
			state.setgCost(state.getgCost() + 1);
			state.setfCost(state.getgCost() + state.gethCost());

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
	private boolean checkOpenList(RCState neighbor, PriorityQueue<RCState> openList) {
		for (RCState check : openList) {
			if (sameState(neighbor,check) &&  neighbor.getfCost() > check.getfCost())
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
	private boolean checkClosedList(RCState neighbor, List<RCState> closedList) {
		for (RCState check : closedList) {
			if (sameState(neighbor,check) && neighbor.getfCost() > check.getfCost())
				return true;
		}
		return false;
	}

	private boolean sameState(RCState one, RCState two){
		Set<String> mapOne = new HashSet<String>();
		Set<String> mapTwo = new HashSet<String>();
		for(String s: one.getState()){
			mapOne.add(s);
		}
		for(String s: two.getState()){
			mapTwo.add(s);
		}
		for(String s:one.getState()){
			if(!mapTwo.contains(s))
				return false;
		}
		for(String s:two.getState()){
			if(!mapOne.contains(s))
				return false;
		}
return true;
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
	private LinkedList<String> path(StateView state) {
		RCStateComparator compare = new RCStateComparator();
		PriorityQueue<RCState> openList = new PriorityQueue<RCState>(10, compare);
		List<RCState> closedList = new LinkedList<RCState>();
		RCState startNode = new RCState(getInitial(),0,0,null);
		openList.add(startNode);
		while (!openList.isEmpty()) {
			RCState temp = openList.poll();
			setNeighbors(temp);
			for (RCState neighbor : temp.getNeighbors()) {
				boolean skip = false;
				if (neighbor.isGoal(woodRequired,goldRequired)) {
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
	public void terminalStep(StateView newstate, History.HistoryView statehistory) {
		step++;

		

	}
	
	public static String getUsage() {
		return "Two arguments, amount of gold to gather and amount of wood to gather";
	}
	@Override
	public void savePlayerData(OutputStream os) {
		//this agent lacks learning and so has nothing to persist.
		
	}
	@Override
	public void loadPlayerData(InputStream is) {
		//this agent lacks learning and so has nothing to persist.
	}
}
