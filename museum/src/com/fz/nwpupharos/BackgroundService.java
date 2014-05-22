package com.fz.nwpupharos;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

public class BackgroundService extends Service {
	private static final String TAG = "@BackgroundService : ";
	// 实际上可以用Handler代替！调用静态方法向Handler发送Msg,但参数数据还是得用Intent传递？！
	private BroadcastReceiver cmdReceiver;
	public static final String ACTION_CMD_RECEIVER = "com.fz.nwpupharos.BackgroundService.receiver";

	public interface CMD {
		int INVALID = 0, STOP_BLUETOOTH_SCAN = 1, START_BLUETOOTH_SCAN = 2;
	}
	private SharedPreferences userPre;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		userPre = getSharedPreferences("userPre", Context.MODE_PRIVATE);
		initCMDReceiver();
	}

	private void initCMDReceiver() {
		cmdReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				int cmd = intent.getIntExtra("cmd", 0);
				switch (cmd) {
				case CMD.INVALID:
					Logger.d(TAG, "INVALID");
					;
					break;
				case CMD.START_BLUETOOTH_SCAN:
					Logger.d(TAG, "START_BLUETOOTH_SCAN");
					if (userPre.getBoolean("locationAware", true)) {
						// 此处相当于做了代理，过滤！
						Logger.d(TAG, "start ble scan.");
						BluetoothLe.getInstance(getApplicationContext()).startContinueScan(500, 1500);
					} else {
						Logger.d(TAG, "user don`t want locationAware,ignore the open BleScan ask!");
					}
					;
					break;
				case CMD.STOP_BLUETOOTH_SCAN:
					Logger.d(TAG, "STOP_BLUETOOTH_SCAN");
					BluetoothLe.getInstance(getApplicationContext()).stopScan();
					;
					break;
				default:
					Logger.d(TAG, "should not be here!");
					break;
				}
			}
		};
		IntentFilter itentf = new IntentFilter(ACTION_CMD_RECEIVER);
		registerReceiver(cmdReceiver, itentf);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Logger.d(TAG, "backgroundService exit!");
		unregisterReceiver(cmdReceiver);
		// ensure quit scan!
		BluetoothLe.getInstance(getApplicationContext()).stopScan();
	}
}
