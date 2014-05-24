package cn.nwpu.museum.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.fz.nwpupharos.BackgroundService;
import com.fz.nwpupharos.MConst;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;

public class Assistant extends FragmentActivity implements MConst {
	private static final String TAG = "@Assistant : ";
	private ViewPager vPager;
	private List<Fragment> fragmentList;
	private MFragmentPagerAdapter fAdapter;
	private ImageButton ttsStart, ttsStop, ttsPause;
	// Actually in the same process,this proxy is the real service`s reference!
	private BackgroundService serviceProxy;
	private boolean bBound = false;
	private ServiceConnection sCon;
	private SharedPreferences userPre;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		userPre= getSharedPreferences("userPre", Context.MODE_PRIVATE);
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
		// >>>>>>>>> bind to BackgroundService
		Intent bindIntent = new Intent();
		bindIntent.setClass(getApplicationContext(), BackgroundService.class);
		bindService(bindIntent, sCon, Context.BIND_AUTO_CREATE);
		// <<<<<<<<<
		setContentView(R.layout.activity_assistant);
		fragmentList = new ArrayList<Fragment>();
		vPager = (ViewPager) findViewById(R.id.exihibitPager);
		for (String dir : pavilionsHtmlAbsDir) {
			Fragment f = creatFragment(dir);
			fragmentList.add(f);
		}
		fAdapter = new MFragmentPagerAdapter(getSupportFragmentManager());
		vPager.setAdapter(fAdapter);
		initViews();
	}

	/**
	 * 显示正在播报的业面
	 */
	private void restoreView() {
		int index = userPre.getInt("pageIndex", 0);
		vPager.setCurrentItem(index);
		vPager.invalidate();	
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

	private void initViews() {
		ttsStart = (ImageButton) findViewById(R.id.ttsStart);
		ttsStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String path = MConst.pavilionsTxtAbsDir[fAdapter.getCurrentPagerIndex()];
				if (bBound) {
					serviceProxy.speekOut(path);
				}
			}
		});
		ttsStop = (ImageButton) findViewById(R.id.ttsStop);
		ttsStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (bBound)
					serviceProxy.stopSpeek();
			}
		});
		ttsPause = (ImageButton) findViewById(R.id.ttsPause);
		ttsPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (bBound)
					serviceProxy.pauseSpeek();
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		restoreView();
	}

	@SuppressLint("ValidFragment")
	public Fragment creatFragment(final String pavilionsAbsDir) {
		Fragment f = new Fragment() {
			@Override
			public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				// TODO Auto-generated method stub
				View v = inflater.inflate(R.layout.fragment_assistant_exhibit, container, false);
				WebView ev = (WebView) v.findViewById(R.id.exhibitwebV);
				File htmlFile = new File(pavilionsAbsDir);
				if (htmlFile.exists()) {
					System.out.println(TAG + "file exist" + ":" + htmlFile.getAbsolutePath());
					ev.loadUrl("file:///" + htmlFile.getAbsolutePath());
				} else {
					System.out.println(TAG + "file  not exist!");
				}
				return v;
			}
		};
		return f;
	}

	class MFragmentPagerAdapter extends FragmentStatePagerAdapter {
		private int currentPosition = 0;

		public MFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fragmentList.size();
		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			System.out.println(TAG + arg0);
			return fragmentList.get(arg0);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
			currentPosition = position - 1;// 显示2个标题，减1
			return pavilionsName[position];
		}

		public int getCurrentPagerIndex() {
			return currentPosition;
		}
	}
}
