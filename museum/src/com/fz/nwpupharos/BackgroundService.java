package com.fz.nwpupharos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import cn.nwpu.museum.activity.R;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.fz.nwpupharos.MessageHelper.ResultListener;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechSynthesizer;
import com.iflytek.speech.SpeechUtility;
import com.iflytek.speech.SynthesizerListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

public class BackgroundService extends Service implements MConst, InitListener {
	private static final String TAG = "@BackgroundService : ";
	private IBinder mBinder = new LocalBinder();// local bind service
	private BroadcastReceiver blReceiver;// bluetooth detected receiver.
	private SpeechSynthesizer mTts = null;// 讯飞语音
	private SynthesizerListener.Stub ttsListener = null;
	private boolean isPaused = false;
	private SharedPreferences userPre;
	private Handler mHandler;
	private FrontiaPushMessageReceiver pushMsgReceiver;
	private static final String API_KEY = "2qsjiR5gxxx1atgwqRbAMBNM";
	public static final int ID_NOTI_SUMMON = 1025;
	// 组长位置更新广播
	public static final String ACTION_LEADER_POSITION_UPDATE = "com.fz.museum.leaderPositionUpdate";
	public static final String EXTRA_MAC_ADD = "com.fz.museum.macAdd";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		userPre = getSharedPreferences("userPre", Context.MODE_PRIVATE);
		initTts();
		initBlReceiver();
		MessageHelper.login();
		mHandler = new Handler();
		initPushMsgReceiver();
		PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, API_KEY);
	}

	/**
	 * init tts engine.
	 */
	private void initTts() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		audioManager.setMicrophoneMute(false);
		audioManager.setSpeakerphoneOn(true);// 使用扬声器外放，即使已经插入耳机
		SpeechUtility.getUtility(this).setAppid("534fb05e");
		mTts = new SpeechSynthesizer(this, this);
		ttsListener = new TtsListener();
		mTts.setParameter(SpeechSynthesizer.SPEED, "50");
		mTts.setParameter(SpeechConstant.DOMAIN, "music");
		mTts.setParameter(SpeechSynthesizer.VOICE_NAME, "xiaoyan");
	}

	private void initBlReceiver() {
		blReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String Mac = intent.getStringExtra(BluetoothLe.EXTRA_MAC_ADDR);
				// Mac is fixed in MConst
				handleMac(Mac);
			}
		};
		IntentFilter iFilter = new IntentFilter(BluetoothLe.ACTION_NODE_DETECTED);
		registerReceiver(blReceiver, iFilter);
	}

	private void initPushMsgReceiver() {
		pushMsgReceiver = new FrontiaPushMessageReceiver() {
			@Override
			public void onUnbind(Context arg0, int arg1, String arg2) {
				// TODO Auto-generated method stub
				Logger.d(TAG, "unBind");
			}

			@Override
			public void onSetTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onNotificationClicked(Context arg0, String arg1, String arg2, String arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onMessage(Context arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub
				Logger.d(TAG, arg1);
				String teamNameAndMac = parsePositionUpdateMSg(arg1);
				// 接收到消息后判断是不是领队位置更新消息，过滤并处理
				if (teamNameAndMac != null) {
					handlePositionUpdate(teamNameAndMac);
				}
			}

			@Override
			public void onListTags(Context arg0, int arg1, List<String> arg2, String arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDelTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onBind(Context arg0, int arg1, String arg2, String arg3, String arg4, String arg5) {
				// TODO Auto-generated method stub
				Logger.e(TAG, "bind to pushService");
			}
		};
		IntentFilter filter = new IntentFilter();
		/*
		 * <!-- 接收 push消息 -->
		 * <action android:name="com.baidu.android.pushservice.action.MESSAGE"
		 * />
		 * <!-- 接收 bind、setTags等method 的返回结果 -->
		 * <action android:name="com.baidu.android.pushservice.action.RECEIVE"
		 * />
		 * <!-- 可选。接受通知点击事件，和通知自定义内容 -->
		 * <action android:name="
		 * com.baidu.android.pushservice.action.notification.CLICK”/>
		 */
		filter.addAction("com.baidu.android.pushservice.action.MESSAGE");
		filter.addAction("com.baidu.android.pushservice.action.RECEIVE");
		filter.addAction("com.baidu.android.pushservice.action.notification.CLICK");
		registerReceiver(pushMsgReceiver, filter);
	}

	private void handlePositionUpdate(String positionMsg) {
		final String SUMMON = "summon";// 领队召集
		Logger.d(TAG, "parsed result---->" + positionMsg);
		if (positionMsg.contains(":")) {
			int indexOfSpilt = positionMsg.indexOf(":");
			String teamName = positionMsg.substring(0, indexOfSpilt).trim();
			String mac = positionMsg.substring(indexOfSpilt + 1).trim();
			// 如果领队发出召集信息，则广播该信息，在groupActivity中接收处理
			if (mac.equals("SUMMON")) {
				if (userPre.getBoolean("isMember", false)) {
					NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					Notification noti = new NotificationCompat.Builder(this).setTicker("领队召集").setContentText("领队召集")
							.setContentTitle("召集").setSmallIcon(R.drawable.ic_launcher)
							.setDefaults(Notification.DEFAULT_ALL).build();
					notiManager.notify(ID_NOTI_SUMMON, noti);
					Intent intent = new Intent(ACTION_LEADER_POSITION_UPDATE);
//					notiManager = null;
					intent.putExtra(EXTRA_MAC_ADD, SUMMON);
					sendBroadcast(intent);
					return;
				}
			}
			Logger.d(TAG, "teamName = " + teamName);
			Logger.d(TAG, "mac = " + mac);
			/**
			 * 1、判断是否是领队，是则忽略该位置更新消息，return
			 * 2、判断是否在队中，不是则忽略该消息，return
			 * 3、不是领队，且在队中，发送领队位置更新广播，有GroupActivity接收，更新领队位置显示
			 */
			if (userPre.getBoolean("isLeader", false)) {
				return;
			}
			if (!userPre.getBoolean("isMember", false) || !teamName.equals(userPre.getString("teamName", null))) {
				return;
			}
			// 在该队伍中,广播“组长位置更新”信息
			Logger.d(TAG, "team match,broadcast !");
			Intent intent = new Intent();
			intent.setAction(ACTION_LEADER_POSITION_UPDATE);
			intent.putExtra(EXTRA_MAC_ADD, mac);
			sendBroadcast(intent);
		}
	}

	private String parsePositionUpdateMSg(String msg) {
		try {
			JSONObject msgJson = new JSONObject(msg);
			String titleBody = msgJson.getString("title");
			return titleBody.toString().trim();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void startBleScan() {
		Logger.d(TAG, "start ble scan.");
		if (userPre.getBoolean("locationAware", true)) {
			BluetoothLe.getInstance(getApplicationContext()).startContinueScan(500, 1500);
		} else {
			Logger.w(TAG, "User prefer to shutdown Bluetooth,won`t start bluetooth scan!");
		}
	}

	public void stopBleScan() {
		Logger.d(TAG, "stop bluetooth scan");
		BluetoothLe.getInstance(getApplicationContext()).stopScan();
	}

	public void speekOut(String TTSContentFilePath) {
		File ttsContentFile = new File(TTSContentFilePath);
		Logger.w(TAG, ttsContentFile.getAbsolutePath());
		if (ttsContentFile.exists()) {
			String ttsContent = null;
			StringBuilder ttsContentBuilder = new StringBuilder();
			// 将文件读入内存
			try {
				BufferedReader input = new BufferedReader(new FileReader(ttsContentFile));
				String temp = null;
				temp = input.readLine();
				while (temp != null) {
					ttsContentBuilder.append(temp);
					temp = input.readLine();
					String t = ttsContentBuilder.toString();
					ttsContent = t.length() > 0 ? t : null;
				}
				input.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mTts != null && ttsContent != null) {
				if (!isPaused) {
					mTts.startSpeaking(ttsContent, ttsListener);
				} else {
					isPaused = false;
					mTts.resumeSpeaking(ttsListener);
				}
			}
		}
	}

	public void pauseSpeek() {
		if (mTts != null && mTts.isSpeaking()) {
			isPaused = true;
			mTts.pauseSpeaking(ttsListener);
		}
	}

	public void stopSpeek() {
		if (mTts != null && mTts.isSpeaking()) {
			isPaused = false;
			mTts.stopSpeaking(ttsListener);
		}
	}

	class TtsListener extends SynthesizerListener.Stub {
		@Override
		public void onBufferProgress(int arg0) throws RemoteException {
			// TODO Auto-generated method stub
		}

		@Override
		public void onCompleted(int arg0) throws RemoteException {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakBegin() throws RemoteException {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakPaused() throws RemoteException {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakProgress(int arg0) throws RemoteException {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakResumed() throws RemoteException {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Logger.d(TAG, "backgroundService exit!");
		BluetoothLe.getInstance(getApplicationContext()).stopScan();
		unregisterReceiver(blReceiver);
		mTts.destory();
		PushManager.stopWork(getApplicationContext());
		unregisterReceiver(pushMsgReceiver);
		// ensure quit scan!
	}

	private void handleMac(String Mac) {
		// TODO Auto-generated method stub
		String pavilion = MacAndInfo.get(Mac);
		Logger.w(TAG, Mac + ":" + pavilion);
		userPre.edit().putInt("pageIndex", MacAndIndex.get(Mac)).commit();
		if (userPre.getBoolean("autoSpeek", true)) {
			// 扫描到展馆，如果自动播报
			speekOut(pavilion);
		}
		if (userPre.getBoolean("isLeader", false)) {
			if (true) {
				Logger.d(TAG, "update current position Mac");
				leaderUpdatePosition(userPre.getString("teamName", null) + ":" + Mac);
			}
		}
	}

	public void leaderUpdatePosition(String Mac) {
		MessageHelper.sendPositionUpdate(new PositionUpdate(Mac), new ResultListener() {
			@Override
			public void onResultSuccess() {
				// TODO Auto-generated method stub
				Logger.d(TAG, "upadate position Mac Successed");
			}

			@Override
			public void onResultFail(String error) {
				// TODO Auto-generated method stub
				Logger.w(TAG, "update position Mac failed!");
			}
		});
	}

	/**
	 * 本机注册为Leader
	 */
	public void registTobeLeader(String teamName) {
		userPre.edit().putBoolean("isLeader", true).putString("teamName", teamName).commit();
//		BluetoothLe.getInstance(getApplicationContext()).stopScan();
//		mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				BluetoothLe.getInstance(getApplicationContext()).startContinueScan(500, 1500);
//			}
//		}, 500);
	}

	/**
	 * 本机不在为团队Leader，注销
	 */
	public void unRegistLeader() {
		userPre.edit().putBoolean("isLeader", false).commit();
	}

	/**
	 * 注册本机为队员
	 */
	public void registTobeMember(String teamName) {
		userPre.edit().putBoolean("isMember", true).putString("teamName", teamName).commit();
	}

	/**
	 * 离队
	 */
	public void unRegistMember() {
		userPre.edit().putBoolean("isMember", false).commit();
	}

	@Override
	public void onInit(ISpeechModule arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	public class LocalBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}
}
