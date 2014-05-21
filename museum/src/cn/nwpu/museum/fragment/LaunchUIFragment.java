package cn.nwpu.museum.fragment;


import cn.nwpu.museum.activity.DescriptionActivity;
import cn.nwpu.museum.activity.MapActivity;
import cn.nwpu.museum.activity.PageViewActivity;
import cn.nwpu.museum.activity.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment; 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
@SuppressLint("ValidFragment")
public class LaunchUIFragment extends Fragment {

	private ImageButton mbtnOpenIR;
	private ImageButton mbtnOpenMap;
	private Context mContext;
		
	public LaunchUIFragment(Context context){
		this.mContext = context;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_selection_launch, container, false);
		mbtnOpenIR = (ImageButton)rootView.findViewById(R.id.openir);
		mbtnOpenIR.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, DescriptionActivity.class);
		        startActivityForResult(intent, 100);
			}
		});
		mbtnOpenMap =  (ImageButton)rootView.findViewById(R.id.openmap);
		mbtnOpenMap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, MapActivity.class);
		        startActivity(intent);
			}
		});
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(20==resultCode)  
        {  
            String tab=data.getExtras().getString("tab");  
            if(tab != null && tab.trim().length() > 0){
            	
            	((PageViewActivity)mContext).selectPage(Integer.parseInt(tab));
            }
        }  
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}
