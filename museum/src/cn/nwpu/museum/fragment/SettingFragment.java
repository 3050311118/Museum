package cn.nwpu.museum.fragment;

import com.fz.nwpupharos.BackgroundService;
import cn.nwpu.museum.activity.R;
import cn.nwpu.museum.activity.SSIDListActivity;
import cn.nwpu.museum.service.PreferenceService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingFragment extends Fragment implements BackgroundService.CMD {
	private ImageButton mImageButton;
	private TextView mSSIDTextView;
	private PreferenceService ps;
	private ToggleButton locationAwareTbtn;
	private SharedPreferences userPre;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		userPre = getActivity().getSharedPreferences("userPre", Context.MODE_PRIVATE);
		View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
		mImageButton = (ImageButton) rootView.findViewById(R.id.btn_wifi_ssid);
		locationAwareTbtn = (ToggleButton) rootView.findViewById(R.id.btn_bluetooth);
		locationAwareTbtn.setChecked(userPre.getBoolean("locationAware", true));
		locationAwareTbtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					userPre.edit().putBoolean("locationAware", true).commit();
					// 告知BackGround service 打开后台扫描
					Intent intent = new Intent(BackgroundService.ACTION_CMD_RECEIVER);
					intent.putExtra("cmd", START_BLUETOOTH_SCAN);
					getActivity().sendBroadcast(intent);
				} else {
					userPre.edit().putBoolean("locationAware", false).commit();
					// 告知BackGround service 关闭后台扫描
					Intent intent = new Intent(BackgroundService.ACTION_CMD_RECEIVER);
					intent.putExtra("cmd", STOP_BLUETOOTH_SCAN);
					getActivity().sendBroadcast(intent);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String ssid = data.getStringExtra("ssid");
		if (ssid != null) {
			mSSIDTextView.setText(ssid);
			ps.saveString(PreferenceService.KEY_SSID, ssid);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
