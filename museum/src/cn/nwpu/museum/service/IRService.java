package cn.nwpu.museum.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

import android.R.integer;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.microcontrollerbg.irdroid.Lirc;

public class IRService {
   
   private IRService(){
	   lirc= new Lirc();
	   parse(LIRCD_CONF_FILE);
   }
   private String TAG="IRService";
   private static IRService irService;
   public static IRService getIRServiceInstance(){
	   if(irService != null) return irService;
	   return new IRService();
   }
    
   private byte buffer[];
   private Lirc lirc;
   public AudioTrack ir;
   public  int bufSize = AudioTrack.getMinBufferSize(48000,
       		AudioFormat.CHANNEL_CONFIGURATION_STEREO,
       		AudioFormat.ENCODING_PCM_8BIT);
	
	private final static String LIRCD_CONF_FILE = "/sdcard/tmp/museumir.conf";
    /**
     * 通过音频口发送红外信号
     */
	public  void  sendSignal(){
		 
         buffer = lirc.getIrBuffer("MY_TV", "VOL+");

		 if (buffer == null) {
		    Log.i("IRService" ,"Empty Buffer!");
		    return;
		 }
		
		 ir = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, 
		  	AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_8BIT, 
		  	bufSize, AudioTrack.MODE_STATIC);

		  if (bufSize < buffer.length)
		     	bufSize = buffer.length;
			
		  ir.write(buffer, 0, buffer.length);
		  ir.setStereoVolume(1,1);
          ir.play();    	
	 }
    /**
     * 从配置文件读取红外按键信息
     * @param config_file
     * @return
     */
    public boolean parse (String config_file) {
    	
    	java.io.File file = new java.io.File(config_file);
    	if (!file.exists()) {
    		if (config_file != LIRCD_CONF_FILE)
    			Log.i(TAG, "The Selected file doesn't exist");
    		else
    			Log.i(TAG,"Configuartion file missing, please update the db");
    		return false;
    	}
    	
    	if (lirc.parse(config_file) == 0) {
 			Log.i(TAG,"Couldn't parse the selected file");
    		return false;
    	}
    	
        String [] str = lirc.getDeviceList();
        for(String device : str){
        	Log.i(TAG,"Device:"+ device);
        }
    	return true;
    }
    
    public void SetConfigure(int ipaddress,Context context){
    	try {
    	    //从asset中读模板
    		AssetManager am = context.getResources().getAssets();
    	    InputStream is = am.open("irconfig.txt");
    		BufferedInputStream inputStream = new BufferedInputStream(is);
    		PrintWriter pWriter = new PrintWriter(new File(LIRCD_CONF_FILE));
    		
    		byte[] data = new byte[600];
    		while(-1 != inputStream.read(data)){
    			String tempString = new String(data);
    			pWriter.write(tempString);
    		}
 
    		Integer ip = (ipaddress >> 16 ) & 0xFFFF;
    		Integer ip_convert = ~ip & 0xFFFF;
    		String ipString = Integer.toHexString(ip).length()==4?
    				Integer.toHexString(ip) : "0"+Integer.toHexString(ip);
    	    String ipCovertString = Integer.toHexString(ip_convert).length()==4?
    	    	  Integer.toHexString(ip_convert) : "0"+Integer.toHexString(ip_convert);   		
		    String configure = "\r\n begin codes \r\n" +
    		    "VOL+     0x"+  
    		     ipString + ipCovertString
                +"  \r\n end codes\r\n end remote";
			pWriter.write(configure);
			pWriter.close();
			inputStream.close();
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
