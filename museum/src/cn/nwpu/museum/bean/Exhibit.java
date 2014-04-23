package cn.nwpu.museum.bean;

import android.graphics.Bitmap;

public class Exhibit {
	
	 private int id; 
	 private String name;
	 private String description;
	 private int exhibitnumber;
	 private int  hallnumber;
	 private Bitmap  bitmap= null;;
	 
	public Exhibit(int id, String name, String description, int exhibitnumber,
			int hallnumber) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.exhibitnumber = exhibitnumber;
		this.hallnumber = hallnumber;
	}
	
	public Exhibit(){}
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getExhibitnumber() {
		return exhibitnumber;
	}
	public void setExhibitnumber(int exhibitnumber) {
		this.exhibitnumber = exhibitnumber;
	}

	public int getHallnumber() {
		return hallnumber;
	}

	public void setHallnumber(int hallnumber) {
		this.hallnumber = hallnumber;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	
}
