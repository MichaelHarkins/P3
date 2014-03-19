import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;


public class RCState {
private int gCost;
private List<String> peasants;
private String action;
private RCState parent;
public int getgCost() {
	return gCost;
}

public void setgCost(int gCost) {
	this.gCost = gCost;
}

public int getfCost() {
	return fCost;
}

public void setfCost(int fCost) {
	this.fCost = fCost;
}

public int gethCost() {
	return hCost;
}

public void sethCost(int hCost) {
	this.hCost = hCost;
}

public List<RCState> getNeighbors() {
	return neighbors;
}

public void setNeighbors(List<RCState> neighbors) {
	this.neighbors = neighbors;
}

public List<String> getState() {
	return state;
}

public void setState(List<String> state) {
	this.state = state;
}
private int fCost;
private int hCost;

private List<RCState> neighbors = new LinkedList<RCState>();
private List<String> state = new LinkedList<String>();

public RCState(List<String> state, int gCost, int hCost){
	for(String s: state){
		state.add(s);
	}
	this.gCost = gCost;
	this.hCost = hCost;
	fCost = gCost + hCost;
}

public RCState clone(){
	RCState c = new RCState(state,getgCost(),gethCost());
	c.setParent(this)

	return c;
}
public boolean Peasant(String peasant){
	return state.contains("Peasant(" + peasant + ")");
}

public boolean Townhall(String townhall){
	return state.contains("Townhall(" + townhall + ")");
}
public boolean Idle(String peasant){
	return state.contains("Idle(" + peasant + ")");
}

public boolean EmptyHanded(String unit){
	return state.contains("Holding(nil," + unit + ")");
}

public boolean HoldingWood(String unit){
	return state.contains("Holding(w," + unit + ")");
}

public boolean HoldingGold(String unit){
	return state.contains("Holding(g," + unit + ")");
}

public boolean Near(String object, String unit){
	return state.contains("Near(" + object + "," + unit + ")");
}

public int getWood(String forest){
	for(String s: state){
		if(s.contains("Wood") && s.contains(forest))
			return Integer.parseInt(s.substring(s.indexOf('('),s.indexOf(',')));
	
	}
	return 0;
}

public int getGold(String mine){
	for(String s: state){
		if(s.contains("Gold") && s.contains(mine))
			return Integer.parseInt(s.substring(s.indexOf('('),s.indexOf(',')));
	}
	return 0;
}

public List<String> getPeasants(){
	List<String> peasants = new LinkedList<String>();
	for(String s: state){
		if(s.contains("Peasant("))
			peasants.add(s.substring(s.indexOf('('),s.indexOf(')')));

	}
}
public boolean HarvestWood(String unit){
	if(Peasant(unit) && Near("f",unit) && EmptyHanded(unit) && Idle(unit))
	{
		state.remove("Holding(nil," + unit);
		state.add("Holding(g,"  + unit  + ")");
		return true;
	}
	return false;
}

public boolean HarvestGold(String mine, String unit){
	if(Peasant(unit) && Near("g",unit) && EmptyHanded(unit) && Idle(unit))
	{
		state.remove("Holding(nil," + unit);
		state.add("Holding(g," + unit + ")");
		return true;
	}
	return false;
}

public boolean DepositGold(String townhall,String unit){
	if(Peasant(unit) && Townhall(townhall) && Near(townhall,unit) && HoldingGold(unit) && Idle(unit) ){
		state.remove("Holding(g," + unit + ")");
		int currentGold = getGold(townhall);
		state.remove("Gold(" + currentGold +  "," + townhall + ")");
		currentGold+= 100;
		state.add("Gold(" + currentGold +  "," + townhall + ")");
		return true;
	}
	else
		return false;
}

public boolean DepositWood(String townhall,String unit){
	if(Peasant(unit) && Townhall(townhall) && Near(townhall,unit) && HoldingWood(unit) && Idle(unit) ){
		state.remove("Holding(w," + unit + ")");
		int currentWood = getWood(townhall);
		state.remove("Wood(" + currentWood +  "," + townhall + ")");
		currentWood+= 100;
		state.add("Wood(" + currentWood +  "," + townhall + ")");
		return true;
	}
	else
		return false;
}
public boolean isNear(String peasant, String unit) {
	for(String s: state){
		if(s.equalsIgnoreCase("Near(" + unit + "," + peasant + ")"))
			return true;
	}
	return false;
}
public boolean GoNear(String peasant, String unit){
	if(Peasant(peasant)){
	int index = -1;
	for(String s: state){
		if(s.equalsIgnoreCase("Near(" + unit + "," + peasant + ")"))
			return false;
		if(s.contains("Near("))
			index = state.indexOf(s);
	}
	if(index != -1)
		state.remove(index);
	state.add("Near(" + unit + "," + peasant + ")");
	return true;
	}
	else
		return false;
}


}
