package cn.nwpu.museum.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.fragment.DesciptionFragment;
import cn.nwpu.museum.service.ExhibitService;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

public class DescriptionActivity extends FragmentActivity implements OnTabChangeListener {

	 private DescriptionPagerAdapter mPagerAdapter;
	 private ViewPager mViewPager;
	 private ImageButton mBtnLeft;
	 private ImageButton mBtnRight;
	 private TabHost mTabs;
	 private int currentPage = 0;
	 private int numofPages = 0;
	 
	 //显示的展品列表
	 private List<exhibit> mExhibits;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_description);
		mPagerAdapter =  new DescriptionPagerAdapter(getSupportFragmentManager());
	    mViewPager = (ViewPager) findViewById(R.id.desc_pager);
	    mViewPager.setAdapter(mPagerAdapter);
	    mBtnLeft = (ImageButton)findViewById(R.id.btnleft);
	    mBtnRight =(ImageButton)findViewById(R.id.btnright);
	    mBtnLeft.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentPage > 0)
			    mViewPager.setCurrentItem(--currentPage);		
			}
		});
	    
       mBtnRight.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentPage < numofPages-1)
			    mViewPager.setCurrentItem(++currentPage);		
			}
		});
       
       mTabs=(TabHost)findViewById(R.id.tabhost);  
       initTab();
       
       //获得展品列表
       Bundle bundle = getIntent().getBundleExtra("HALL");
       mExhibits = new ArrayList<exhibit>();
       if(bundle != null){
    	   String hallnumber = bundle.getString("HALLNUMBER");
    	   int selected = bundle.getInt("SELECTED_NUMBER");
    	   ExhibitService es = new ExhibitService(this);
   		   for(Exhibit exhibit :   es.findByHall(hallnumber)){
	    	   mExhibits.add(new exhibit(null, String.valueOf(exhibit.getExhibitnumber())) );
	       }
       }else{
       }
       //测试  --连通后删除
       Button btn_test = (Button)this.findViewById(R.id.btntest);
       btn_test.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			numofPages++;
			currentPage= numofPages-1;
			mExhibits.add(new exhibit(null, String.valueOf(currentPage+1 )));
			mViewPager.setCurrentItem(currentPage);
		}
	    });
	}
	
	//包含展品编号和相关展品的编号
    public class exhibit implements Comparable<Object> {
    	public int[] relatedex;
    	public String exhibitnumber;
    	public exhibit(int [] r, String e){
    		this.relatedex =r;
    		this.exhibitnumber = e;
    	}
		@Override
		public int compareTo(Object e) {
			
			exhibit ex = (exhibit) e;
			if(Integer.parseInt(this.exhibitnumber) > Integer.parseInt(ex.exhibitnumber)){
				return 1;
			}
			return 0;
		}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_description, menu);
		return true;
	}
	
    private void initTab(){
		
		mTabs.setup();  
        TabHost.TabSpec spec=mTabs.newTabSpec("0"); 
        spec.setContent(R.id.tab1);  
        spec.setIndicator("首页", this.getResources().getDrawable(R.drawable.ic_home));
        mTabs.addTab(spec);  
      
        spec=mTabs.newTabSpec("1");  
        spec.setContent(R.id.tab2);  
        spec.setIndicator("馆藏", this.getResources().getDrawable(R.drawable.ic_exhibition));
        mTabs.addTab(spec);
        
        spec=mTabs.newTabSpec("2");  
        spec.setContent(R.id.tab3);  
        spec.setIndicator("地图", this.getResources().getDrawable(R.drawable.ic_map)); 
        mTabs.addTab(spec);
        
        spec=mTabs.newTabSpec("3");  
        spec.setContent(R.id.tab4);  
        spec.setIndicator("其它", this.getResources().getDrawable(R.drawable.ic_home));
        mTabs.addTab(spec);
        
        //设置字体大小
        TabWidget tabWidget = mTabs.getTabWidget();
        int count = tabWidget.getChildCount();//TabHost中有一个getTabWidget()的方法
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
	public class DescriptionPagerAdapter extends FragmentStatePagerAdapter {
	    public DescriptionPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }

	    @Override
	    public Fragment getItem(int i) {
	        Fragment fragment = new DesciptionFragment(mExhibits.get(i));
	        Bundle args = new Bundle();
	        // Our object is just an integer :-P
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
	    Intent data=new Intent();  
        data.putExtra("tab", arg0);   
        setResult(20, data);
		this.finish();
	}


}
