package cn.nwpu.museum.activity;

import cn.nwpu.museum.fragment.HallFragment;
import cn.nwpu.museum.fragment.LaunchUIFragment;
import cn.nwpu.museum.fragment.SettingFragment;
import cn.nwpu.museum.services.UDPService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;;

public class PageViewActivity extends FragmentActivity implements OnTabChangeListener {

	private ViewPager mViewPager;
	private TabFragmentPagerAdapter mAdapter;  
	private TabHost mTabs;
	@Override
	public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_page_view);  
        
        mViewPager = (ViewPager) this.findViewById(R.id.pager);
        mTabs=(TabHost)findViewById(R.id.tabhost);  
        initTab();
        initView();
        startUDPService();
    }  
	
	private void initTab(){
		
		mTabs.setup();  
        TabHost.TabSpec spec=mTabs.newTabSpec("0"); 
        spec.setContent(R.id.tab1);  
        spec.setIndicator("��ҳ", this.getResources().getDrawable(R.drawable.ic_home));
        mTabs.addTab(spec);  
      
        spec=mTabs.newTabSpec("1");  
        spec.setContent(R.id.tab2);  
        spec.setIndicator("�ݲ�", this.getResources().getDrawable(R.drawable.ic_exhibition));
        mTabs.addTab(spec);
        
        spec=mTabs.newTabSpec("2");  
        spec.setContent(R.id.tab3);  
        spec.setIndicator("��ͼ", this.getResources().getDrawable(R.drawable.ic_map)); 
        mTabs.addTab(spec);
        
        spec=mTabs.newTabSpec("3");  
        spec.setContent(R.id.tab4);  
        spec.setIndicator("����", this.getResources().getDrawable(R.drawable.ic_home));
        mTabs.addTab(spec);
        
        //���������С
        TabWidget tabWidget = mTabs.getTabWidget();
        int count = tabWidget.getChildCount();//TabHost����һ��getTabWidget()�ķ���
        for (int i = 0; i < count; i++) {
           View view = tabWidget.getChildTabViewAt(i);   
           final TextView tv = (TextView) view.findViewById(android.R.id.title);
           tv.setTextSize(18);
           tv.setTextColor(this.getResources().getColorStateList( 
                   android.R.color.white)); 
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

    public void startUDPService(){
    	Intent UDPintent = new Intent(this, UDPService.class);
		startService(UDPintent);
    }
	public  class TabFragmentPagerAdapter extends FragmentPagerAdapter{
	
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
				ft = new LaunchUIFragment(PageViewActivity.this);
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
	        
			switch(position){
			
			case 0: return "��ҳ";
			case 1: return "�ݲ�";
			case 2: return "��ͼ"; 
			case 3: return "����";
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
		getMenuInflater().inflate(R.menu.activity_page_view, menu);
		return true;
	}
	
	
	public void selectPage(int tab){
		mViewPager.setCurrentItem(tab);
		mTabs.setCurrentTab(tab);
	}
	
}
