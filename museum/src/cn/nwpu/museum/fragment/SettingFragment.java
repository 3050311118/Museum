package cn.nwpu.museum.fragment;

import cn.nwpu.museum.activity.R;
import cn.nwpu.museum.activity.SSIDListActivity;
import cn.nwpu.museum.service.PreferenceService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class SettingFragment extends Fragment {
	
	private ImageButton mImageButton;
	private TextView mSSIDTextView;
	private PreferenceService ps;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_setting, container, false);	
		mImageButton = (ImageButton)rootView.findViewById(R.id.btn_wifi_ssid);
		mImageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), SSIDListActivity.class);
				startActivityForResult(intent, 100);
			}
		});
		mSSIDTextView =(TextView)rootView.findViewById(R.id.tx_title_wifi_ssid);
		ps = new PreferenceService(getActivity());
		return rootView;
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String ssid = data.getStringExtra("ssid");
		if(ssid != null){
			mSSIDTextView.setText(ssid);
			ps.saveString(PreferenceService.KEY_SSID, ssid);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
