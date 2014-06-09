package cn.nwpu.museum.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.nwpu.museum.bean.Hall;
import cn.nwpu.museum.db.DBManager;
import cn.nwpu.museum.db.DBOpenHelper;

public class HallService {

	//private DBOpenHelper dbOpenHelper;
	private DBManager dbManager; 
    private SQLiteDatabase db;
	
    private void openDB(){
        this.db = dbManager.openDatabase();
     }
     
     private void closeDB(){
     	dbManager.closeDatabase();    	
     }

    public HallService(Context context){
		//this.dbOpenHelper = new DBOpenHelper(context);
    	this.dbManager = new DBManager(context);
 		openDB();
	}
	
	public void save(Hall hall){
		//SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL("insert into hall(name,positionx,positiony,hallnumber) values(?,?,?,?)",
				  new Object[]{hall.getName(),hall.getPositionx(),hall.getPositiony(),hall.getHallnumber()});
	}
	
	
	public void update(Hall hall){
		//SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL("update hall set name=?,positionx=?,positiony=? where hallid=?",
				new Object[]{hall.getName(),hall.getPositionx(),hall.getPositiony(),hall.getId()});
	}
	
	public void delete(Integer index){
		//SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
	    db.execSQL("delete from hall where hallid=?",new Object[]{index});
	}
	
	public Hall find(Integer index){
		//SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery("select * from hall where hallid=?", new String[]{index.toString()});
	    if(cursor.moveToFirst()){
	    	int id = cursor.getInt(cursor.getColumnIndex("hallid"));
	        String name = cursor.getString(cursor.getColumnIndex("name"));
	        int postionx = cursor.getInt(cursor.getColumnIndex("positionx"));
	        int postiony = cursor.getInt(cursor.getColumnIndex("positiony"));
	        int hallnumber = cursor.getInt(cursor.getColumnIndex("hallnumber"));
	        return new Hall(id,name,postionx,postiony,hallnumber);
	    }
	    cursor.close();
	    return null;
	}
	
	public Hall findByNumber(Integer number){
		//SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery("select * from hall where hallnumber=?", new String[]{number.toString()});
	    if(cursor.moveToFirst()){
	    	int id = cursor.getInt(cursor.getColumnIndex("hallid"));
	        String name = cursor.getString(cursor.getColumnIndex("name"));
	        int postionx = cursor.getInt(cursor.getColumnIndex("positionx"));
	        int postiony = cursor.getInt(cursor.getColumnIndex("positiony"));
	        int hallnumber = cursor.getInt(cursor.getColumnIndex("hallnumber"));
	        return new Hall(id,name,postionx,postiony,hallnumber);
	    }
	    cursor.close();
	    return null;
	}
	
	public List<Hall> getAll(){
		List<Hall> halls = new ArrayList<Hall>();
		//SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from hall order by hallid asc", null);
	    while(cursor.moveToNext()){
	    	int id = cursor.getInt(cursor.getColumnIndex("hallid"));
	        String name = cursor.getString(cursor.getColumnIndex("name"));
	        int postionx = cursor.getInt(cursor.getColumnIndex("positionx"));
	        int postiony = cursor.getInt(cursor.getColumnIndex("positiony"));
	        int hallnumber = cursor.getInt(cursor.getColumnIndex("hallnumber"));
	    	halls.add(new Hall(id,name,postionx,postiony,hallnumber));
	    }
	    return halls;
	}
	
	public long getCount(){
		
		//SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(*) from hall", null);
		cursor.moveToFirst();
		long result = cursor.getLong(0);
		cursor.close();
		return result;
	}
}
