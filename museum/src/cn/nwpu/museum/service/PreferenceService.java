package cn.nwpu.museum.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.StaticLayout;
import android.util.Log;

public class PreferenceService {
	 
	   private final String TAG ="PreferenceService";
	   public final static String KEY_SSID = "ssid";
	   private SharedPreferences sharedPreferences;
	   public PreferenceService(Context context){
		   sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);;
	   }
	      
	   public void saveString(String key , String value){
		   
		   Log.i(TAG,"save");
		   Editor editor = sharedPreferences.edit();
	       editor.putString(key, value);
		   editor.commit();
	   }
	   
	   public String getString(String key){
		   return sharedPreferences.getString(KEY_SSID, "");
	   }
}
