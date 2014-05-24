package cn.nwpu.museum.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

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
   public AudioTrack ir = null;
   public  int bufSize = AudioTrack.getMinBufferSize(48000,
       		AudioFormat.CHANNEL_CONFIGURATION_STEREO,
       		AudioFormat.ENCODING_PCM_8BIT);
	
	private Timer mTimer = null;
	private IRTimerThread irthread;
	
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
		
		  if (bufSize < buffer.length)
		     	bufSize = buffer.length;
		 // while(ir != null && ir.getPlayState()==AudioTrack.PLAYSTATE_PLAYING ){}
		  if(ir != null){
			  ir.flush();
		      ir.release();
		  }
		  ir = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, 
					  	AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_8BIT, 
					  	bufSize, AudioTrack.MODE_STATIC);
		  
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
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    		PrintWriter pWriter = new PrintWriter(new File(LIRCD_CONF_FILE));
    		
    		//byte[] data = new byte[600];
    		String data;
    		Integer ip = (ipaddress >> 16 ) & 0xFFFF;
    		Integer ip_convert = ~ip & 0xFFFF;
    		String ipString = Integer.toHexString(ip).length()==4?
    				Integer.toHexString(ip) : "0"+Integer.toHexString(ip);
    	    String ipCovertString = Integer.toHexString(ip_convert).length()==4?
    	    	  Integer.toHexString(ip_convert) : "0"+Integer.toHexString(ip_convert);   
    		while(null != (data = reader.readLine())){
    			//String tempString = new String(data);
    			if(data.contains("pre_data") && !data.contains("pre_data_bits")){
    				data = data.substring(0, 10) + "    0x" + ipString;
    			}else if (data.contains("VOL+")){
    				data = "          VOL+   0x" + ipCovertString;
    			}
    			pWriter.println(data);
    		}

			pWriter.close();
			reader.close();
			//重新解析
			this.parse(LIRCD_CONF_FILE);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public class IRTimerThread extends TimerTask{

		@Override
		public void run() {
			try {
				Log.i(TAG,"sendir");
				sendSignal();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
    }
    
   /**
    *  开启红外发射
    * @param interval 单位ms
    */
    public void StartIRThread(long interval){
    	if(mTimer == null){
    		mTimer = new Timer();
    	}
        irthread = new IRTimerThread();
    	mTimer.schedule(irthread, 0, interval);  
    }
   
    public void StopIRThread(){
    	if(mTimer != null){
    		mTimer.cancel();
    		mTimer = null;
    	}
    	if(irthread != null){
    		irthread.cancel();
    	} 
       if(ir != null && ir.getPlayState()==AudioTrack.PLAYSTATE_PLAYING){
    	   ir.stop();
    	   ir.release();
       }
    }
    
    public void SendIROnce(){
    	sendSignal();
    }
}
