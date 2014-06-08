package cn.nwpu.museum.fragment;

import com.fz.nwpupharos.BackgroundService;
import cn.nwpu.museum.activity.R;
import cn.nwpu.museum.activity.SSIDListActivity;
import cn.nwpu.museum.service.PreferenceService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingFragment extends Fragment {
	private ImageButton mImageButton;
	private TextView mSSIDTextView;
	private PreferenceService ps;
	private ToggleButton locationAwareTbtn, autoSpeekBtn;
	private SharedPreferences userPre;
	// Actually in the same process,this proxy is the real service`s reference!
	private BackgroundService serviceProxy;
	private boolean bBound = false;
	private ServiceConnection sCon;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		userPre = getActivity().getSharedPreferences("userPre", Context.MODE_PRIVATE);
		View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
		mImageButton = (ImageButton) rootView.findViewById(R.id.btn_wifi_ssid);
		locationAwareTbtn = (ToggleButton) rootView.findViewById(R.id.btn_bluetooth);
		locationAwareTbtn.setChecked(userPre.getBoolean("locationAware", true));
		autoSpeekBtn = (ToggleButton) rootView.findViewById(R.id.btn_autoSpeek);
		autoSpeekBtn.setChecked(userPre.getBoolean("autoSpeek", true));
		autoSpeekBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				userPre.edit().putBoolean("autoSpeek", isChecked).commit();
			}
		});
		locationAwareTbtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					userPre.edit().putBoolean("locationAware", true).commit();
					// 告知BackGround service 打开后台扫描
					if (bBound)
						serviceProxy.startBleScan();
				} else {
					userPre.edit().putBoolean("locationAware", false).commit();
					// 告知BackGround service 关闭后台扫描
					if (bBound)
						serviceProxy.stopBleScan();
				}
			}
		});
		mImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), SSIDListActivity.class);
				startActivityForResult(intent, 100);
			}
		});
		mSSIDTextView = (TextView) rootView.findViewById(R.id.tx_title_wifi_ssid);
		ps = new PreferenceService(getActivity());
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
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
		bindIntent.setClass(getActivity(), BackgroundService.class);
		getActivity().bindService(bindIntent, sCon, Context.BIND_AUTO_CREATE);
		// <<<<<<<<<
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		if (bBound) {
			bBound = false;
			getActivity().unbindService(sCon);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String ssid = data.getStringExtra("ssid");
		if (ssid != null) {
			mSSIDTextView.setText(ssid);
			ps.saveString(PreferenceService.KEY_SSID, ssid);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
