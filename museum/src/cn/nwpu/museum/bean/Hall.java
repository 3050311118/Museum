package cn.nwpu.museum.bean;

public class Hall {
    private int id;
    private String name;
    private int positionx;
    private int positiony;
    private int hallnumber;
    
	public Hall(int id, String name, int positionx,
			int positiony, int hallnumber) {
		super();
		this.id = id;
		this.name = name;
		this.positionx = positionx;
		this.positiony = positiony;
		this.hallnumber = hallnumber;
	}
	
	public Hall(){}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPositionx() {
		return positionx;
	}
	public void setPositionx(int positionx) {
		this.positionx = positionx;
	}
	public int getPositiony() {
		return positiony;
	}
	public void setPositiony(int positiony) {
		this.positiony = positiony;
	}

	public int getHallnumber() {
		return hallnumber;
	}

	public void setHallnumber(int hallnumber) {
		this.hallnumber = hallnumber;
	}

    
}
