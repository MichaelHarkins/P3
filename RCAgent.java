import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	
	private int step;
	
	public RCAgent(int playernum, String[] arguments) {
		super(playernum);
		
		goldRequired = Integer.parseInt(arguments[0]);
		woodRequired = Integer.parseInt(arguments[1]);
	}

	StateView currentState;
	
	/**
	 * This takes all of the players peasants and makes them collect gold
	 * 
	 * @param peasantIDs
	 *            the list of the players peasants
	 * @param townHallIDs
	 *            the list of the townhalls to transfer the gold to
	 * @param builder
	 *            the list of integer to action to tell the peasants to collect
	 *            gold
	 */
	private void collectGold(List<Integer> peasantIDs,
			List<Integer> townHallIDs, Map<Integer, Action> builder) {

		for (int peasantId : peasantIDs) {
			int townHallId = townHallIDs.get(0);
			Action b = null;
			if (currentState.getUnit(peasantId).getCargoType() == ResourceType.GOLD
					&& currentState.getUnit(peasantId).getCargoAmount() > 0)
				b = new TargetedAction(peasantId, ActionType.COMPOUNDDEPOSIT,
						townHallId);
			else {
				List<Integer> resourceIDs = currentState
						.getResourceNodeIds(Type.GOLD_MINE);
				b = new TargetedAction(peasantId, ActionType.COMPOUNDGATHER,
						resourceIDs.get(0));
			}
			builder.put(peasantId, b);
		}

	}
	
	/**
	 * This takes all of the players peasants and makes them collect wood
	 * 
	 * @param peasantIDs
	 *            the list of the players peasants
	 * @param townHallIDs
	 *            the list of the townhalls to transfer the wood to
	 * @param builder
	 *            the list of integer to action to tell the peasants to collect
	 *            wood
	 */
	private void collectWood(List<Integer> peasantIDs,
			List<Integer> townHallIDs, Map<Integer, Action> builder) {
		for (int peasantId : peasantIDs) {
			int townHallId = townHallIDs.get(0);
			Action b = null;
			if (currentState.getUnit(peasantId).getCargoAmount() > 0)
				b = new TargetedAction(peasantId, ActionType.COMPOUNDDEPOSIT,
						townHallId);
			else {
				List<Integer> resourceIDs = currentState
						.getResourceNodeIds(Type.TREE);
				b = new TargetedAction(peasantId, ActionType.COMPOUNDGATHER,
						resourceIDs.get(0));
			}
			builder.put(peasantId, b);
		}
	}

	/**
	 * Puts of all the players units in lists as well as all of the enemy units
	 * 
	 * @param peasantIDs
	 *            the list that the peasants ids will go into
	 * @param townHallIDs
	 *            the list that the townhall ids will go into
	 * @param farmIDs
	 *            the list that the farm ids will go into
	 * @param barracksIDs
	 *            the list that the barracks ids will go into
	 * @param footmanIDs
	 *            the list that the footman ids will go into
	 * @param enemyUnits
	 *            the list that the enemy units will go into
	 */
	private void getIDs(List<Integer> peasantIDs, List<Integer> townHallIDs,
			List<Integer> farmIDs, List<Integer> barracksIDs,
			List<Integer> footmanIDs, List<Integer> enemyUnits) {
		List<Integer> allUnitIDs = currentState.getAllUnitIds();
		List<Integer> myUnits = currentState.getUnitIds(playernum);
		for (int i = 0; i < allUnitIDs.size(); i++) {
			int id = allUnitIDs.get(i);
			UnitView unit = currentState.getUnit(id);
			String unitTypeName = unit.getTemplateView().getName();
			if (unitTypeName.equals("TownHall") && myUnits.contains(id))
				townHallIDs.add(id);
			else if (unitTypeName.equals("Peasant") && myUnits.contains(id))
				peasantIDs.add(id);
			else if (unitTypeName.equals("Farm") && myUnits.contains(id))
				farmIDs.add(id);
			else if (unitTypeName.equals("Barracks") && myUnits.contains(id))
				barracksIDs.add(id);
			else if (unitTypeName.equals("Footman") && myUnits.contains(id))
				footmanIDs.add(id);
			else
				enemyUnits.add(id);
		}

	}

	/**
	 * Creates a structure from a given template id for that structure
	 * 
	 * @param peasantID
	 *            the peasant to build the structure
	 * @param templateID
	 *            the template of the structure to build
	 * @param builder
	 *            the map to create the action
	 */
	public void buildStructure(int peasantID, int templateID,
			Map<Integer, Action> builder) {
		builder.put(peasantID,
				Action.createPrimitiveBuild(peasantID, templateID));
	}

	/**
	 * Creates a unit given a unit and building id
	 * 
	 * @param buildingID
	 *            the building id to create the unit from
	 * @param unitID
	 *            the id of the unit to create
	 * @param builder
	 *            the map to create the action
	 */
	private void createUnit(int buildingID, int unitID,
			Map<Integer, Action> builder) {
		builder.put(buildingID,
				Action.createCompoundProduction(buildingID, unitID));

	}

	/**
	 * This is a complicated function that will collect resouces until it can
	 * build/create what it is told to
	 * 
	 * @param peasantID
	 *            the id of the peasant to build if it is building a structure
	 * @param buildingID
	 *            the building to create the unit
	 * @param structure
	 *            the structure to build if building
	 * @param peasantIDs
	 *            the list of the peasats to collect resouces
	 * @param townHallIDs
	 *            the list of the townhalls to return the resources to
	 * @param builder
	 *            the map of actions to queue the actions
	 * @param currentGold
	 *            the current gold of the player
	 * @param currentWood
	 *            the current wood of the player
	 * @param building
	 *            if the person is building a structure or creating a unit, true
	 *            if building, false if structure
	 */
	private void collectResourcesAndBuild(int peasantID, int buildingID,
			String structure, List<Integer> peasantIDs,
			List<Integer> townHallIDs, Map<Integer, Action> builder,
			int currentGold, int currentWood, boolean building) {
		TemplateView structureTemplate = currentState.getTemplate(playernum,
				structure);
		if (currentWood < structureTemplate.getWoodCost())
			collectWood(peasantIDs, townHallIDs, builder);
		else if (currentGold < structureTemplate.getGoldCost())
			collectGold(peasantIDs, townHallIDs, builder);
		else {
			if (building)
				buildStructure(peasantID, structureTemplate.getID(), builder);
			else
				createUnit(buildingID, structureTemplate.getID(), builder);
		}

	}

	/**
	 * Makes one unit attack another
	 * 
	 * @param attackingUnit
	 *            the unit making the attack
	 * @param unitToAttacke
	 *            unit being attacked
	 * @param builder
	 *            the builder to map the actions to
	 */
	private void attackUnit(int attackingUnit, int unitToAttack,
			Map<Integer, Action> builder) {
		builder.put(attackingUnit,
				Action.createCompoundAttack(attackingUnit, unitToAttack));
	}


	public List<String> getInitial(){
		List<String> initial = new LinkedList<String>();
		initial.add("Peasant(p1)");
		initial.add("Idle(p1)");
		initial.add("Townhall(t1)");
		initial.add("Mine(g1)");
		initial.add("Gold(100,g1)");
		initial.add("Mine(g2)");
		initial.add("Gold(500,g2)");
		initial.add("Mine(g3)");
		initial.add("Gold(5000,g3)");
		initial.add("Forest(f1)");
		initial.add("Wood(400,f1)");
		initial.add("Forest(f2)");
		initial.add("Wood(400,f2)");
		initial.add("Forest(f3)");
		initial.add("Wood(400,f3)");
		initial.add("Forest(f4)");
		initial.add("Wood(400,f4)");
		initial.add("Forest(f5)");
		initial.add("Wood(400,f5)");
		initial.add("Holding(nil,p1)");
		initial.add("Near(t1,p1)");
		initial.add("Gold(0,t1)");
		initial.add("Wood(0,t1)");
		return initial;
		
	}
	public List<String> goal(int wood,int gold){
	 List<String> goal = new LinkedList<String>();
	 goal.add("Gold(" +gold + ",t1)");
	 goal.add("Wood(" + wood + ",t1)");
	 return goal;
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

	public void heuristic()
	{
		
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
