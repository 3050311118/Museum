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
	public static final String ACTION_LEADER_MSG = "com.fz.museum.leaderMsg";
	public static final String EXTRA_MAC_OR_CMD = "com.fz.museum.macOrCmd";

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
		initBleReceiver();
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

	/**
	 * 注册蓝牙扫描Receiver
	 */
	private void initBleReceiver() {
		blReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String Mac = intent.getStringExtra(BluetoothLe.EXTRA_MAC_ADDR);
				// Mac is fixed in MConst
				handleScannedMac(Mac);
			}
		};
		IntentFilter iFilter = new IntentFilter(BluetoothLe.ACTION_NODE_DETECTED);
		registerReceiver(blReceiver, iFilter);
	}

	/**
	 * 处理蓝牙扫描到的节点Mac
	 * 
	 * @param Mac
	 */
	private void handleScannedMac(String Mac) {
		// TODO Auto-generated method stub
		// 过滤掉扫描到“野节点判断”！
		if (MacAndIndex.get(Mac) != null) {
			String pavilion = MacAndInfo.get(Mac);
			Logger.w(TAG, Mac + ":" + pavilion);
			userPre.edit().putInt("pageIndex", MacAndIndex.get(Mac)).commit();// 缓存展馆位置，进入展馆PageView是自动切换到对应页
			userPre.edit().putInt("myPosition", MacAndIndex.get(Mac)).commit();// 缓存本机位置
			if (userPre.getBoolean("autoSpeek", true)) {
				// 扫描到展馆，如果自动播报
				speekOut(pavilion);
			}
			// 如果本机是Leader的话，则广播自己位置更新
			if (userPre.getBoolean("isLeader", false)) {
				if (true) {
					Logger.d(TAG, "update leader position");
					sendLeaderMsg(userPre.getString("teamName", null) + ":" + Mac);
				}
			}
		}
	}

	/**
	 * 发送LeaderMsg，目前用到的有两种消息:
	 * 1、Leader位置更新消息 "teameName:mac(ad34123df123)"
	 * 2、Leader召集队员消息"teameName:summon"
	 * 
	 * @param Mac
	 */
	public void sendLeaderMsg(String leaderMsg) {
		MessageHelper.sendPositionUpdate(new PositionUpdate(leaderMsg), new ResultListener() {
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
	 * 注册推送信息Receiver，过滤只接收来自Leader的消息
	 */
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
				String leaderMsg = parseLeaderMsg(arg1);
				// 接收到消息后判断是不是LeaderMsg
				if (leaderMsg != null) {
					handleLeaderMsg(leaderMsg);
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
		filter.addAction("com.baidu.android.pushservice.action.MESSAGE");
		filter.addAction("com.baidu.android.pushservice.action.RECEIVE");
		filter.addAction("com.baidu.android.pushservice.action.notification.CLICK");
		registerReceiver(pushMsgReceiver, filter);
	}

	/**
	 * 处理领队发来的消息：1、召集消息 2、领队位置更新消息
	 * 
	 * @param leaderSendMsg
	 */
	private void handleLeaderMsg(String leaderSendMsg) {
		final String SUMMON = "summon";// 领队召集
		Logger.d(TAG, "parsed result---->" + leaderSendMsg);
		if (leaderSendMsg.contains(":")) {
			int indexOfSpilt = leaderSendMsg.indexOf(":");
			String teamName = leaderSendMsg.substring(0, indexOfSpilt).trim();
			String macOrCmd = leaderSendMsg.substring(indexOfSpilt + 1).trim();
			Logger.d(TAG, "teamName = " + teamName);
			Logger.d(TAG, "macOrCmd = " + macOrCmd);
			/**
			 * 判断是否在消息指定的队伍中且不是领队（避免自发自收！）
			 * 如果是分情况处理，处理完后将消息直接广播，在GroupActivity中进一步区分处理
			 */
			if (teamName.equals(userPre.getString("teamName", null)) && !userPre.getBoolean("isLeader", false)) {
				if (macOrCmd.equals(SUMMON)) {
					Logger.d(TAG, "领队召集消息！");
//					NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//					Notification noti = new NotificationCompat.Builder(this).setTicker("领队召集").setContentText("领队召集")
//							.setContentTitle("召集").setSmallIcon(R.drawable.ic_launcher)
//							.setDefaults(Notification.DEFAULT_ALL).build();
//					notiManager.notify(ID_NOTI_SUMMON, noti);
				} else {
					Logger.d(TAG, "领队位置更新消息！");
					// 此处接收到的Mac不会为野节点！
					userPre.edit().putInt("leaderPosition", MacAndIndex.get(macOrCmd)).commit();// 缓存leader位置
				}
				Intent intent = new Intent();
				intent.setAction(ACTION_LEADER_MSG);
				intent.putExtra(EXTRA_MAC_OR_CMD, macOrCmd);
				sendBroadcast(intent);
			}
		}
	}

	/**
	 * 从Json字符串中获得该应用感兴趣的消息体
	 * 此处是套用NWPUPharos的遗留问题！
	 * 
	 * @param msg
	 * @return
	 */
	private String parseLeaderMsg(String msg) {
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

	/**
	 * 开启BLE扫描
	 */
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
