package cn.nwpu.museum.activity;

import java.util.ArrayList;
import java.util.List;
import com.fz.nwpupharos.BackgroundService;
import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.fragment.DesciptionFragment;
import cn.nwpu.museum.fragment.WifiDialogFragment;
import cn.nwpu.museum.service.ExhibitService;
import cn.nwpu.museum.service.IRService;
import cn.nwpu.museum.service.PreferenceService;
import cn.nwpu.museum.services.UDPService;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * 
 * @author liuruofeng
 * 
 */
public class DescriptionActivity extends FragmentActivity implements OnTabChangeListener {
	private final String TAG = "DescriptionActivity";
	private DescriptionPagerAdapter mPagerAdapter;
	private ViewPager mViewPager;
	private ImageButton mBtnLeft;
	private ImageButton mBtnRight;
	private TabHost mTabs;
	private int currentPage = 0;
	private int numofPages = 0;
	private Bundle bundle;
	private IRService irService;
	private String SSID = "Connectify-me";
	private IntentFilter mWifiStateIntentFilter;
	private BroadcastReceiver mWifiStateReceiver;
	private final int MESSAGE_ADD_EXHIBIT = 0;
	// 显示的展品列表
	private List<exhibit> mExhibits;
	private PreferenceService ps;
	// Actually in the same process,this proxy is the real service`s reference!
	private BackgroundService serviceProxy;
	private boolean bBound = false;
	private ServiceConnection sCon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// >>>>>>>>> bind to BackgroundService
		sCon = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				bBound = false;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				BackgroundService.LocalBinder lb = (BackgroundService.LocalBinder) service;
				serviceProxy = lb.getService();
				bBound = true;
			}
		};
		// >>>>>>>>>>>
		Intent bindIntent = new Intent();
		bindIntent.setClass(getApplicationContext(), BackgroundService.class);
		bindService(bindIntent, sCon, Context.BIND_AUTO_CREATE);
		//<<<<<<<<<<<<
		setContentView(R.layout.activity_description);
		irService = IRService.getIRServiceInstance();
		mPagerAdapter = new DescriptionPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.desc_pager);
		mViewPager.setAdapter(mPagerAdapter);
		mBtnLeft = (ImageButton) findViewById(R.id.btnleft);
		mBtnRight = (ImageButton) findViewById(R.id.btnright);
		mBtnLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentPage > 0)
					mViewPager.setCurrentItem(--currentPage);
			}
		});
		mBtnRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentPage < numofPages - 1)
					mViewPager.setCurrentItem(++currentPage);
			}
		});
		// 初始化tab
		mTabs = (TabHost) findViewById(R.id.tabhost);
		initTab();
		// 获得展品列表
		bundle = getIntent().getBundleExtra("HALL");
		mExhibits = new ArrayList<exhibit>();
		if (bundle != null) {
			String hallnumber = bundle.getString("HALLNUMBER");
			int selected = bundle.getInt("SELECTED_NUMBER");
			ExhibitService es = new ExhibitService(this);
			for (Exhibit exhibit : es.findByHall(hallnumber)) {
				mExhibits.add(new exhibit(null, String.valueOf(exhibit.getExhibitnumber())));
			}
		} else {
			// 红外模式，要使用wifi,监听wifi状态变化
			mWifiStateIntentFilter = new IntentFilter();
			mWifiStateIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			mWifiStateIntentFilter.addAction(UDPService.broadIntentString);
			mWifiStateReceiver = new WifiStateReceiver();
		}
		// 测试 --连通后删除
		ImageButton btn_test = (ImageButton) this.findViewById(R.id.btntest);
		btn_test.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 停止播放音频
				stopAudio();
			}
		});
		ps = new PreferenceService(this);
	}
	private MediaPlayer playerSound;
	private boolean audioStopFlag;

	private void stopAudio() {
		// 停止播放音频
		if (playerSound != null && playerSound.isPlaying()) {
			playerSound.stop();
			// playerSound.release();
			audioStopFlag = true;
		}
	}

	@Override
	protected void onPause() {
		if (bundle == null) {
			unregisterReceiver(mWifiStateReceiver);
			stopAudio();
			irService.StopIRThread();
		}
		super.onPause();
		// 打开蓝牙扫描
		if (bBound) {
			serviceProxy.startBleScan();
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (bBound) {
			bBound = false;
			unbindService(sCon);
		}
	}

	@Override
	protected void onResume() {
		// 红外模式
		if (bundle == null) {
			// 监听wifi状态变化
			registerReceiver(mWifiStateReceiver, mWifiStateIntentFilter);
			// 监听wifi接 收数据
			// Intent UDPintent = new Intent(this, UDPService.class);
			// startService(UDPintent);
			// 判断wifi当前的状态
			WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo wifiInfo = wifi_service.getConnectionInfo();
			SSID = ps.getString(PreferenceService.KEY_SSID);
			if (wifiInfo == null || wifiInfo.getSSID() == null) {
				// || !(wifiInfo.getSSID().equals(SSID)
				WifiDialogFragment wf = new WifiDialogFragment();
				wf.show(getSupportFragmentManager(), "wifi设置");
			} else {
				// 得到ip地址，写入配置文件中
				int ipaddress = wifiInfo.getIpAddress();
				// 开启红外
				String ip = "";
				ip += (ipaddress >> 24);
				ip += ":" + ((ipaddress & 0x00FF0000) >> 16);
				ip += ":" + ((ipaddress & 0x0000FF00) >> 8);
				ip += ":" + (ipaddress & 0x0000FF);
				Log.i(TAG, "ip:" + ip);
				irService.SetConfigure(ipaddress, this);
				irService.StartIRThread(1000);
			}
		}
		super.onResume();
		// 关闭蓝牙扫描
		if (bBound) {
			serviceProxy.stopBleScan();
		}
	}

	// 包含展品编号和相关展品的编号
	public class exhibit implements Comparable<Object> {
		public int[] relatedex;
		public String exhibitnumber;

		public exhibit(int[] r, String e) {
			this.relatedex = r;
			this.exhibitnumber = e;
		}

		@Override
		public int compareTo(Object e) {
			exhibit ex = (exhibit) e;
			if (Integer.parseInt(this.exhibitnumber) > Integer.parseInt(ex.exhibitnumber)) {
				return 1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof exhibit) {
				exhibit ex = (exhibit) o;
				if (ex.exhibitnumber.equals(this.exhibitnumber)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_description, menu);
		return true;
	}

	// 添加一个新的展品页
	private void addExhibit(String exhibitnumber) {
		exhibit newEx = new exhibit(null, exhibitnumber);
		if (mExhibits.contains(newEx)) {
			currentPage = mExhibits.indexOf(newEx);
		} else {
			numofPages++;
			currentPage = numofPages - 1;
			mExhibits.add(newEx);
		}
		playAudio(exhibitnumber);
		mViewPager.setCurrentItem(currentPage);
	}
	// 初始化handler
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// 此方法在ui线程运行
			switch (msg.what) {
			case MESSAGE_ADD_EXHIBIT:
				String exhibit = msg.getData().getString("EXHIBITNUMBER");
				addExhibit(exhibit.substring(0, 1));
				break;
			}
		}
	};

	private void beep() {
		Log.i(TAG, "beep");
		Thread beepThread = new Thread() {
			@Override
			public void run() {
				irService.StopIRThread();
				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audioManager.setMicrophoneMute(false);
				audioManager.setSpeakerphoneOn(true);// 使用扬声器外放，即使已经插入耳机
				setVolumeControlStream(AudioManager.STREAM_MUSIC);// 控制声音的大小
				audioManager.setMode(AudioManager.STREAM_MUSIC);
				MediaPlayer playerSound = MediaPlayer.create(DescriptionActivity.this, R.raw.beep);
				playerSound.start();
				while (playerSound.isPlaying())
					;
				audioManager.setMicrophoneMute(true);
				audioManager.setSpeakerphoneOn(false);
				audioManager.setMode(AudioManager.STREAM_SYSTEM);
				setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);// 控制声音的大小
				irService.StartIRThread(1500);
				;
			}
		};
		beepThread.start();
	}

	// 播放语音介绍线程
	class AudioThread extends Thread {
		private String exhibitnum;

		public AudioThread(String exhibit) {
			this.exhibitnum = exhibit;
		}

		@Override
		public void run() {
			irService.StopIRThread();
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
			audioManager.setMicrophoneMute(false);
			audioManager.setSpeakerphoneOn(true);// 使用扬声器外放，即使已经插入耳机
			// MediaPlayer playerSound =
			// MediaPlayer.create(DescriptionActivity.this, R.raw.exhibit1);
			playerSound = new MediaPlayer();
			ExhibitService es = new ExhibitService(DescriptionActivity.this);
			try {
				AssetFileDescriptor fileDescriptor = es.getAudioPath(exhibitnum);
				playerSound.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(),
						fileDescriptor.getLength());
				playerSound.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置流类型
				playerSound.setLooping(false); // 设置是否循环播放
				playerSound.prepare();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			audioStopFlag = false;
			playerSound.start();
			while (playerSound.isPlaying() && !audioStopFlag)
				;
			playerSound.release();
			playerSound = null;
			audioManager.setSpeakerphoneOn(false);
			audioManager.setMicrophoneMute(true);
			audioManager.setMode(AudioManager.MODE_NORMAL);
			irService.StartIRThread(1000);
		}
	};

	// 播放音频介绍
	private void playAudio(String exhibitnum) {
		Log.i(TAG, "playAudio");
		AudioThread thread = new AudioThread(exhibitnum);
		thread.start();
	}

	private void initTab() {
		mTabs.setup();
		TabHost.TabSpec spec = mTabs.newTabSpec("0");
		spec.setContent(R.id.tab1);
		spec.setIndicator("首页", this.getResources().getDrawable(R.drawable.ic_home));
		mTabs.addTab(spec);
		spec = mTabs.newTabSpec("1");
		spec.setContent(R.id.tab2);
		spec.setIndicator("馆藏", this.getResources().getDrawable(R.drawable.ic_exhibition));
		mTabs.addTab(spec);
		spec = mTabs.newTabSpec("2");
		spec.setContent(R.id.tab3);
		spec.setIndicator("地图", this.getResources().getDrawable(R.drawable.ic_map));
		mTabs.addTab(spec);
		spec = mTabs.newTabSpec("3");
		spec.setContent(R.id.tab4);
		spec.setIndicator("其它", this.getResources().getDrawable(R.drawable.ic_home));
		mTabs.addTab(spec);
		// 设置字体大小
		TabWidget tabWidget = mTabs.getTabWidget();
		int count = tabWidget.getChildCount();// TabHost中有一个getTabWidget()的方法
		for (int i = 0; i < count; i++) {
			View view = tabWidget.getChildTabViewAt(i);
			final TextView tv = (TextView) view.findViewById(android.R.id.title);
			tv.setTextSize(18);
			tv.setTextColor(this.getResources().getColorStateList(android.R.color.white));
		}
		mTabs.setOnTabChangedListener(this);
		mTabs.setCurrentTab(0);
	}

	public class DescriptionPagerAdapter extends FragmentStatePagerAdapter {
		public DescriptionPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new DesciptionFragment(mExhibits.get(i));
			Bundle args = new Bundle();
			args.putInt(DesciptionFragment.ARG_OBJECT, i + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return mExhibits.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "展品" + (position + 1);
		}
	}

	@Override
	public void onTabChanged(String arg0) {
		Intent data = new Intent();
		data.putExtra("tab", arg0);
		setResult(20, data);
		this.finish();
	}

	/**
	 * 监听wifi状态的变化，如果wifi断了要提示重连，否则无法接收推送
	 * 根据wifi状态决定是否开启红外定时发送任务。
	 * 
	 * @author liuruofeng
	 * 
	 */
	public class WifiStateReceiver extends BroadcastReceiver {
		private String TAG = "WifiStateReceiver";

		@Override
		/*
		 * WifiManager.WIFI_STATE_DISABLING 正在停止
		 * WifiManager.WIFI_STATE_DISABLED 已停止
		 * WifiManager.WIFI_STATE_ENABLING 正在打开
		 * WifiManager.WIFI_STATE_ENABLED 已开启
		 * WifiManager.WIFI_STATE_UNKNOWN 未知
		 */
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "receiverBroad");
			String action = intent.getAction();
			// 接收wifi变化和wifi接收到数据两种广播，这里为wifi数据广播
			if (action.equals(UDPService.broadIntentString)) {
				String exhibit = intent.getStringExtra("EXHIBITNUMBER");
				Log.i(TAG, "getDataBroadCast:" + exhibit);
				Message msg = new Message();
				msg.what = 0;
				Bundle bundle = new Bundle();
				bundle.putString("EXHIBITNUMBER", exhibit);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
				return;
			}
			// 否则，为wifi变化广播
			WifiDialogFragment wf = new WifiDialogFragment();
			switch (intent.getIntExtra("wifi_state", 0)) {
			case WifiManager.WIFI_STATE_DISABLING:
				Log.d(TAG, "WIFI STATUS : WIFI_STATE_DISABLING");
				wf.show(getSupportFragmentManager(), "wifi设置");
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				Log.d(TAG, "WIFI STATUS : WIFI_STATE_DISABLED");
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				Log.d(TAG, "WIFI STATUS : WIFI_STATE_ENABLING");
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				Log.d(TAG, "WIFI STATUS : WIFI_STATE_ENABLED");
				// 得到ip地址，写入配置文件中
				WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifi_service.getConnectionInfo();
				int ipaddress = wifiInfo.getIpAddress();
				// 开启红外
				irService.SetConfigure(ipaddress, DescriptionActivity.this);
				break;
			case WifiManager.WIFI_STATE_UNKNOWN:
				Log.d(TAG, "WIFI STATUS : WIFI_STATE_UNKNOWN");
				wf.show(getSupportFragmentManager(), "wifi设置");
				break;
			}
		}
	}
}
