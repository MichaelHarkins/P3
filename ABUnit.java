
public class ABUnit {
	private int x;
	private int y;
	private int id;
	public boolean isEmpty() {
		return empty;
	}


	public void setEmpty(boolean empty) {
		this.empty = empty;
	}


	public boolean hasGold() {
		return gold;
	}


	public void setGold(boolean gold) {
		this.gold = gold;
	}


	public boolean hasWood() {
		return wood;
	}


	public void setWood(boolean wood) {
		this.wood = wood;
	}

	private boolean empty;
	private boolean gold;
	private boolean wood;
	public ABUnit(int x, int y, int id, boolean empty, boolean gold, boolean wood){
		this.x = x;
		this.y = y;
		this.id = id;
		this.empty = empty;
		this.gold = gold;
		this.wood = wood;
	}


	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = (int)x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = (int)y;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


}
