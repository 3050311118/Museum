package cn.nwpu.museum.fragment;

import java.util.ArrayList;
import java.util.List;

import cn.nwpu.museum.activity.DescriptionActivity;
import cn.nwpu.museum.activity.R;
import cn.nwpu.museum.adapter.ExhibitAdapter;
import cn.nwpu.museum.bean.Exhibit;
import cn.nwpu.museum.bean.Hall;
import cn.nwpu.museum.service.ExhibitService;
import cn.nwpu.museum.service.HallService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.AdapterView;

public class HallFragment extends Fragment implements OnTabChangeListener, AdapterView.OnItemClickListener{

	private TabHost mTabs;
	private HallService hallService;
	private ListView  mList;
	private List<Exhibit> mExhibits = new ArrayList<Exhibit>();
	private ExhibitAdapter mExhibitAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_hall, container, false);
		//初始化展馆列表
		mTabs=(TabHost)rootView;
		initTab();
		//初始化展品列表 
		mList=(ListView)mTabs.findViewById(R.id.list);
		mExhibitAdapter = new ExhibitAdapter(this.getActivity(), mExhibits, R.layout.list_item);
		
		mList.setAdapter(mExhibitAdapter);
		mList.setOnItemClickListener(this);
		onTabChanged("0");
		mTabs.setCurrentTab(1); //直接设置Tab0，显示不出listview内容
		mTabs.setCurrentTab(0); 
		return rootView;
	}
	private void initTab(){
        
		mTabs.setup();  
		// 初始化标签卡，内容为展厅hall 
		hallService = new HallService(this.getActivity());
		for(Hall hall : hallService.getAll()){
			TabHost.TabSpec spec=mTabs.newTabSpec(String.valueOf(hall.getHallnumber())); 
	        spec.setContent(R.id.list);  
	        spec.setIndicator(hall.getName());
	        mTabs.addTab(spec);  
		}
	   TabWidget tabWidget = mTabs.getTabWidget();
	        int count = tabWidget.getChildCount();//TabHost中有一个getTabWidget()的方法
	        for (int i = 0; i < count; i++) {
	           View view = tabWidget.getChildTabViewAt(i);   
	           final TextView tv = (TextView) view.findViewById(android.R.id.title);
	           tv.setTextSize(14);
	           tv.setTextColor(this.getResources().getColorStateList(android.R.color.white)); 
	        }
        mTabs.setOnTabChangedListener(this);
	  
		
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public void onTabChanged(String index) {
	    //实现index号展馆的所有展品	
		ExhibitService es = new ExhibitService(this.getActivity());
		mExhibits.clear();
        mExhibits.addAll(es.findByHall(index));
		Log.i("HallFragment", index + " number:" + mExhibits.size());
		mExhibitAdapter.notifyDataSetChanged();
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
		//显示展品详细信息
		Intent intent = new Intent(this.getActivity(), DescriptionActivity.class);
		//bundle里放要显示的展品列表
		Bundle bundle = new Bundle();
		/*for(Exhibit ex :mExhibits){
			bundle.putIntArray(String.valueOf(ex.getExhibitnumber()), null);
		}*/
		bundle.putString("HALLNUMBER", mTabs.getCurrentTabTag());
		bundle.putInt("SELECTED_NUMBER", mExhibits.get(index).getExhibitnumber());
		intent.putExtra("HALL", bundle);
		//intent.putExtra("EXHIBITS", bundle);
		//intent.putExtra("HALLNUMBER", mTabs.getCurrentTabTag());
		//intent.putExtra("SELECTED_NUMBER", mExhibits.get(index).getExhibitnumber());
		this.startActivity(intent);
	}
}
