package cn.nwpu.museum.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.db.DBOpenHelper;

public class ExhibitService {

    private DBOpenHelper dbOpenHelper;
	
    private Context context;
    /**
     *   Exhibit:                      columm
     *   private int id;               exhibitid
		 private String name;          name
		 private String description;   description
		 private int exhibitnumber;    exhibitnumber
		 private int  hallid;          hallid
     * @param context
     */
	public ExhibitService(Context context){
		this.dbOpenHelper = new DBOpenHelper(context);
		this.context = context;
	}
	
	public void save(Exhibit exhibit){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL("insert into exhibit(name,description,exhibitnumber,hallnumber) values(?,?,?,?)",
				  new Object[]{exhibit.getName(),exhibit.getDescription(),exhibit.getExhibitnumber(),exhibit.getHallnumber()});
	}
	
	
	public void update(Exhibit exhibit){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL("update exhibit set name=?,description=?,exhibitnumber=? hallnumber=? where exhibitid=?",
				new Object[]{exhibit.getName(),exhibit.getDescription(),exhibit.getExhibitnumber(),exhibit.getHallnumber()});
	}
	
	public void delete(Integer index){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
	    db.execSQL("delete from exhibit where exhibitid=?",new Object[]{index});
	}
	
	public Exhibit find(Integer index){
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery("select * from exhibit where exhibitid=?", new String[]{index.toString()});
	    if(cursor.moveToFirst()){
	    	int id = cursor.getInt(cursor.getColumnIndex("exhibitid"));
	        String name = cursor.getString(cursor.getColumnIndex("name"));
	        String description = cursor.getString(cursor.getColumnIndex("description"));
	        int exhibitnumber = cursor.getInt(cursor.getColumnIndex("exhibitnumber"));
	        int hallnumber = cursor.getInt(cursor.getColumnIndex("hallnumber"));
	        return new Exhibit(id,name,description,exhibitnumber,hallnumber);
	    }
	    cursor.close();
	    return null;
	}
	
	public Exhibit findByNumber(Integer number){
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery("select * from exhibit where exhibitnumber=?", new String[]{number.toString()});
	    if(cursor.moveToFirst()){
	    	int id = cursor.getInt(cursor.getColumnIndex("exhibitid"));
	        String name = cursor.getString(cursor.getColumnIndex("name"));
	        String description = cursor.getString(cursor.getColumnIndex("description"));
	        int exhibitnumber = cursor.getInt(cursor.getColumnIndex("exhibitnumber"));
	        int hallnumber = cursor.getInt(cursor.getColumnIndex("hallnumber"));
	        return new Exhibit(id,name,description,exhibitnumber,hallnumber);
	    }
	    cursor.close();
	    return null;
	}
	
	/**
	 * 查找所有展馆号为hallnumber的展品
	 * @param hallnumber
	 * @return
	 */
	public List<Exhibit> findByHall(String hallnumber){
		List<Exhibit> Exhibits = new ArrayList<Exhibit>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from exhibit where hallnumber=? order by exhibitnumber asc", new String[]{hallnumber});
	    while(cursor.moveToNext()){
	    	int id = cursor.getInt(cursor.getColumnIndex("exhibitid"));
	        String name = cursor.getString(cursor.getColumnIndex("name"));
	        String description = cursor.getString(cursor.getColumnIndex("description"));
	        int exhibitnumber = cursor.getInt(cursor.getColumnIndex("exhibitnumber"));
	        int hallNumber = cursor.getInt(cursor.getColumnIndex("hallnumber"));
	        Exhibits.add(new Exhibit(id,name,description,exhibitnumber,hallNumber));
	    }
	    return Exhibits;
	}
	public List<Exhibit> getAll(){
		List<Exhibit> Exhibits = new ArrayList<Exhibit>();
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from exhibit order by exhibitnumber asc", null);
	    while(cursor.moveToNext()){
	    	int id = cursor.getInt(cursor.getColumnIndex("exhibitid"));
	        String name = cursor.getString(cursor.getColumnIndex("name"));
	        String description = cursor.getString(cursor.getColumnIndex("description"));
	        int exhibitnumber = cursor.getInt(cursor.getColumnIndex("exhibitnumber"));
	        int hallnumber = cursor.getInt(cursor.getColumnIndex("hallnumber"));
	        Exhibits.add(new Exhibit(id,name,description,exhibitnumber,hallnumber));
	    }
	    return Exhibits;
	}
	
	public long getCount(){
		
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(*) from exhibit", null);
		cursor.moveToFirst();
		long result = cursor.getLong(0);
		cursor.close();
		return result;
	}
	
	public Bitmap getImageFromAssetsFile(String fileName)
    {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try
        {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return image;
    }
}
