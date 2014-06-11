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
	private SpeechSynthesizer mTts = null;// Ѷ������
	private SynthesizerListener.Stub ttsListener = null;
	private boolean isPaused = false;
	private SharedPreferences userPre;
	private Handler mHandler;
	private FrontiaPushMessageReceiver pushMsgReceiver;
	private static final String API_KEY = "2qsjiR5gxxx1atgwqRbAMBNM";
	public static final int ID_NOTI_SUMMON = 1025;
	// �鳤λ�ø��¹㲥
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
		audioManager.setSpeakerphoneOn(true);// ʹ����������ţ���ʹ�Ѿ��������
		SpeechUtility.getUtility(this).setAppid("534fb05e");
		mTts = new SpeechSynthesizer(this, this);
		ttsListener = new TtsListener();
		mTts.setParameter(SpeechSynthesizer.SPEED, "50");
		mTts.setParameter(SpeechConstant.DOMAIN, "music");
		mTts.setParameter(SpeechSynthesizer.VOICE_NAME, "xiaoyan");
	}

	/**
	 * ע������ɨ��Receiver
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
	 * ��������ɨ�赽�Ľڵ�Mac
	 * 
	 * @param Mac
	 */
	private void handleScannedMac(String Mac) {
		// TODO Auto-generated method stub
		// ���˵�ɨ�赽��Ұ�ڵ��жϡ���
		if (MacAndIndex.get(Mac) != null) {
			String pavilion = MacAndInfo.get(Mac);
			Logger.w(TAG, Mac + ":" + pavilion);
			userPre.edit().putInt("pageIndex", MacAndIndex.get(Mac)).commit();// ����չ��λ�ã�����չ��PageView���Զ��л�����Ӧҳ
			userPre.edit().putInt("myPosition", MacAndIndex.get(Mac)).commit();// ���汾��λ��
			if (userPre.getBoolean("autoSpeek", true)) {
				// ɨ�赽չ�ݣ�����Զ�����
				speekOut(pavilion);
			}
			// ���������Leader�Ļ�����㲥�Լ�λ�ø���
			if (userPre.getBoolean("isLeader", false)) {
				if (true) {
					Logger.d(TAG, "update leader position");
					sendLeaderMsg(userPre.getString("teamName", null) + ":" + Mac);
				}
			}
		}
	}

	/**
	 * ����LeaderMsg��Ŀǰ�õ�����������Ϣ:
	 * 1��Leaderλ�ø�����Ϣ "teameName:mac(ad34123df123)"
	 * 2��Leader�ټ���Ա��Ϣ"teameName:summon"
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
	 * ע��������ϢReceiver������ֻ��������Leader����Ϣ
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
				// ���յ���Ϣ���ж��ǲ���LeaderMsg
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
	 * ������ӷ�������Ϣ��1���ټ���Ϣ 2�����λ�ø�����Ϣ
	 * 
	 * @param leaderSendMsg
	 */
	private void handleLeaderMsg(String leaderSendMsg) {
		final String SUMMON = "summon";// ����ټ�
		Logger.d(TAG, "parsed result---->" + leaderSendMsg);
		if (leaderSendMsg.contains(":")) {
			int indexOfSpilt = leaderSendMsg.indexOf(":");
			String teamName = leaderSendMsg.substring(0, indexOfSpilt).trim();
			String macOrCmd = leaderSendMsg.substring(indexOfSpilt + 1).trim();
			Logger.d(TAG, "teamName = " + teamName);
			Logger.d(TAG, "macOrCmd = " + macOrCmd);
			/**
			 * �ж��Ƿ�����Ϣָ���Ķ������Ҳ�����ӣ������Է����գ���
			 * ����Ƿ�����������������Ϣֱ�ӹ㲥����GroupActivity�н�һ�����ִ���
			 */
			if (teamName.equals(userPre.getString("teamName", null)) && !userPre.getBoolean("isLeader", false)) {
				if (macOrCmd.equals(SUMMON)) {
					Logger.d(TAG, "����ټ���Ϣ��");
//					NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//					Notification noti = new NotificationCompat.Builder(this).setTicker("����ټ�").setContentText("����ټ�")
//							.setContentTitle("�ټ�").setSmallIcon(R.drawable.ic_launcher)
//							.setDefaults(Notification.DEFAULT_ALL).build();
//					notiManager.notify(ID_NOTI_SUMMON, noti);
				} else {
					Logger.d(TAG, "���λ�ø�����Ϣ��");
					// �˴����յ���Mac����ΪҰ�ڵ㣡
					userPre.edit().putInt("leaderPosition", MacAndIndex.get(macOrCmd)).commit();// ����leaderλ��
				}
				Intent intent = new Intent();
				intent.setAction(ACTION_LEADER_MSG);
				intent.putExtra(EXTRA_MAC_OR_CMD, macOrCmd);
				sendBroadcast(intent);
			}
		}
	}

	/**
	 * ��Json�ַ����л�ø�Ӧ�ø���Ȥ����Ϣ��
	 * �˴�������NWPUPharos���������⣡
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
	 * ����BLEɨ��
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
			// ���ļ������ڴ�
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
	 * ����ע��ΪLeader
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
	 * ��������Ϊ�Ŷ�Leader��ע��
	 */
	public void unRegistLeader() {
		userPre.edit().putBoolean("isLeader", false).commit();
	}

	/**
	 * ע�᱾��Ϊ��Ա
	 */
	public void registTobeMember(String teamName) {
		userPre.edit().putBoolean("isMember", true).putString("teamName", teamName).commit();
	}

	/**
	 * ���
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
