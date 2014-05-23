package cn.nwpu.museum.activity;

import com.fz.nwpupharos.BackgroundService;
import cn.nwpu.museum.fragment.HallFragment;
import cn.nwpu.museum.fragment.LaunchUIFragment;
import cn.nwpu.museum.fragment.MemoFragment;
import cn.nwpu.museum.fragment.SettingFragment;
import cn.nwpu.museum.services.UDPService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

;
public class PageViewActivity extends FragmentActivity implements OnTabChangeListener {
	private ViewPager mViewPager;
	private TabFragmentPagerAdapter mAdapter;
	private TabHost mTabs;
	// Actually in the same process,this proxy is the real service`s reference!
	private BackgroundService serviceProxy;
	private boolean bBound = false;
	private ServiceConnection sCon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		setContentView(R.layout.activity_page_view);
		mViewPager = (ViewPager) this.findViewById(R.id.pager);
		mTabs = (TabHost) findViewById(R.id.tabhost);
		initTab();
		initView();
		startUDPService();
		startBackgroundService();
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

	private void startBackgroundService() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), BackgroundService.class);
		startService(intent);
		// 发送开启蓝牙扫描请求
		if (bBound) {
			serviceProxy.startBleScan();
		}
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
		spec.setIndicator("留言墙", this.getResources().getDrawable(R.drawable.ic_map));
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

	private void initView() {
		mAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				Log.i("swip", "position" + arg0);
				mTabs.setCurrentTab(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	public void startUDPService() {
		Intent UDPintent = new Intent(this, UDPService.class);
		startService(UDPintent);
	}

	public class TabFragmentPagerAdapter extends FragmentPagerAdapter {
		public TabFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment ft = null;
			switch (arg0) {
			case 0:
				ft = new LaunchUIFragment(PageViewActivity.this);
				break;
			case 1:
				ft = new HallFragment();
				break;
			case 2:
				ft = new MemoFragment();
				break;
			case 3:
				ft = new SettingFragment();
				break;
			}
			return ft;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "首页";
			case 1:
				return "馆藏";
			case 2:
				return "地图";
			case 3:
				return "其它";
			}
			return "";
		}
	}

	@Override
	public void onTabChanged(String tabId) {
		this.mViewPager.setCurrentItem(Integer.parseInt(tabId));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem mi = menu.add("退出后台服务");
		mi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), BackgroundService.class);
				stopService(intent);
				finish();
				return true;
			}
		});
		getMenuInflater().inflate(R.menu.activity_page_view, menu);
		return true;
	}

	public void selectPage(int tab) {
		mViewPager.setCurrentItem(tab);
		mTabs.setCurrentTab(tab);
	}
}
