import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.cwru.sepia.environment.model.state.Unit.UnitView;


public class RCState {
private int gCost;
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

public boolean Mine(String mine){
	return state.contains("Mine(" + mine + ")");
	
}
public boolean Forest(String forest){
	return state.contains("Forest(" + forest + ")");
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

public boolean HarvestWood(String forest, String unit){
	if(Forest(forest) && Peasant(unit) && Near(forest,unit) && EmptyHanded(unit) && Idle(unit))
	{
		state.remove("Peasant(" + unit + ")");
		state.remove("Holding(nil," + unit);
		int currentWood = getWood(forest);
		state.remove("Wood("+currentWood + ")");
		currentWood-= 100;
		if(currentWood == 0){
			state.remove("Forest(" + forest + ")");
		}
		else
			state.add("Wood(" +currentWood + ")");
		return true;
	}
	return false;
}

public boolean HarvestGold(String mine, String unit){
	if(Mine(mine) && Peasant(unit) && Near(mine,unit) && EmptyHanded(unit) && Idle(unit))
	{
		state.remove("Holding(nil," + unit);
		int currentGold = getGold(mine);
		state.remove("Gold("+currentGold + ")");
		currentGold-= 100;
		if(currentGold == 0)
			state.remove("Mine(" + mine + ")");
		else
			state.add("Gold(" +currentGold + ")");
		state.add("Holding(g," + unit + ")");
		return true;
	}
	return false;
}

public boolean DepositGold(String townhall,String unit){
	if(Peasant(unit) && Townhall(townhall) && Near(townhall,unit) && HoldingGold(unit) && Idle(unit) ){
		state.remove("Holding(g," + unit + ")");
		int currentGold = getGold(townhall);
		state.remove("Gold(" + currentGold + ")");
		currentGold+= 100;
		state.add("Gold(" + currentGold + ")");
		return true;
	}
	else
		return false;
}

public boolean DepositWood(String townhall,String unit){
	if(Peasant(unit) && Townhall(townhall) && Near(townhall,unit) && HoldingWood(unit) && Idle(unit) ){
		state.remove("Holding(w," + unit + ")");
		int currentWood = getWood(townhall);
		state.remove("Wood(" + currentWood + ")");
		currentWood+= 100;
		state.add("Wood(" + currentWood + ")");
		return true;
	}
	else
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
