package cn.nwpu.museum.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

public class UDPService extends Service implements Runnable {

	private boolean UDPlisten = true;
	private String TAG ="UDPService";
    private  WifiManager.MulticastLock lock;
    public static String broadIntentString ="cn.nwpu.museum.services.UDPSerivce.BroadCast";
    
 
    public void stopListen(){
    	UDPlisten = false;
    }
    
    
    @Override
	public void onCreate() {
    	
    	super.onCreate();
    	WifiManager manager = (WifiManager)getSystemService(WIFI_SERVICE);
 	    this.lock= manager.createMulticastLock("UDPwifi");    
    	Thread thread = new Thread(this);
    	thread.start();
		
	}

	@Override
	public void onDestroy() {
		this.stopListen();
		super.onDestroy();
	}


    
	public void StartListen()  {
	     Integer port = 8903;
	     byte[] message = new byte[100];
	     try {
	        // 建立Socket连接
	        DatagramSocket datagramSocket = new DatagramSocket(port);
	        DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
	        datagramSocket.setBroadcast(false);
	        try {
	           this.lock.acquire();
	           while (UDPlisten) {
	            // 准备接收数据
	            Log.i(TAG, "准备接收UDP报文");     
	            datagramSocket.receive(datagramPacket);
	            String strMsg=new String(datagramPacket.getData()).trim();
	            Log.i(TAG, datagramPacket.getAddress().getHostAddress().toString()+ ":" +strMsg );
	            //将接收到的数据广播出去，activity收到会刷新页面
	            Intent intent =new Intent();
	            intent.setAction(broadIntentString);
	            intent.putExtra("EXHIBITNUMBER", strMsg);
	            this.sendBroadcast(intent);
	           }
	           this.lock.release();
	         } catch (IOException e) {//IOException
	                e.printStackTrace();
	         }
	        } catch (SocketException e) {
	            e.printStackTrace();
	        }
           
	    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void run() {
		UDPlisten = true;
		this.StartListen();
	}
}
