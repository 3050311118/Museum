package com.fz.nwpupharos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
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
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

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
	// �鳤λ�ø��¹㲥
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
				// ���յ���Ϣ���ж��ǲ������λ�ø�����Ϣ�����˲�����
				if (true) {
					// ����ڶ�����
					String teamNameAndMac = parsePositionUpdateMSg(arg1);
					Logger.d(TAG, "parsed result:" + teamNameAndMac);
					if (teamNameAndMac != null && teamNameAndMac.contains(":")) {
						int indexOfSpilt = teamNameAndMac.indexOf(":");
						String teamName = teamNameAndMac.substring(0, indexOfSpilt).trim();
						String mac = teamNameAndMac.substring(indexOfSpilt+1).trim();
						Logger.d(TAG, "teamName = " + teamName);
						Logger.d(TAG, "mac = " + mac);
						if (teamName.equals(userPre.getString("teamName", null))) {
							// �ڸö�����,�㲥���鳤λ�ø��¡���Ϣ
							Logger.d(TAG, "team match,broadcast !");
							Intent intent = new Intent();
							intent.setAction(ACTION_LEADER_POSITION_UPDATE);
							intent.putExtra(EXTRA_MAC_ADD, mac);
							sendBroadcast(intent);
						}
					}
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
		 * <!-- ���� push��Ϣ -->
		 * <action android:name="com.baidu.android.pushservice.action.MESSAGE"
		 * />
		 * <!-- ���� bind��setTags��method �ķ��ؽ�� -->
		 * <action android:name="com.baidu.android.pushservice.action.RECEIVE"
		 * />
		 * <!-- ��ѡ������֪ͨ����¼�����֪ͨ�Զ������� -->
		 * <action android:name="
		 * com.baidu.android.pushservice.action.notification.CLICK��/>
		 */
		filter.addAction("com.baidu.android.pushservice.action.MESSAGE");
		filter.addAction("com.baidu.android.pushservice.action.RECEIVE");
		filter.addAction("com.baidu.android.pushservice.action.notification.CLICK");
		registerReceiver(pushMsgReceiver, filter);
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

	private void handleMac(String Mac) {
		// TODO Auto-generated method stub
		String pavilion = MacAndInfo.get(Mac);
		Logger.w(TAG, Mac + ":" + pavilion);
		speekOut(pavilion);
//		if (userPre.getBoolean("leader", false)) {
		if(true){
			Logger.d(TAG, "update current position Mac");
			leaderUpdatePosition(userPre.getString("teamName", null) + ":" + Mac);
		}
	}

	private void leaderUpdatePosition(String Mac) {
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
	 * ����ע��ΪLeader
	 */
	public void registTobeLeader(String teamName) {
		userPre.edit().putBoolean("leader", true).putString("teamName", teamName).commit();
		BluetoothLe.getInstance(getApplicationContext()).stopScan();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				BluetoothLe.getInstance(getApplicationContext()).startContinueScan(500, 1500);
			}
		}, 500);
	}

	/**
	 * ��������Ϊ�Ŷ�Leader��ע��
	 */
	public void unRegistLeader() {
		userPre.edit().putBoolean("leader", false).commit();
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
