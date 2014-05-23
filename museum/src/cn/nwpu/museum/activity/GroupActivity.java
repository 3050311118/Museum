package cn.nwpu.museum.activity;

import com.fz.nwpupharos.BackgroundService;
import com.fz.nwpupharos.BluetoothLe;
import com.fz.nwpupharos.Logger;
import com.fz.nwpupharos.MConst;
import com.fz.nwpupharos.MapView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GroupActivity extends Activity {
	private ToggleButton btnLeader, btnMember;
	private Button btnSummon;
	private static final String TAG = "GroupActivity";
	// Actually in the same process,this proxy is the real service`s reference!
	private BackgroundService serviceProxy;
	private boolean bBound = false;
	private ServiceConnection sCon;
	private MapView mapView;
	private BroadcastReceiver positionChangedReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
		initReceiver();
		initViews();
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
		Intent bindIntent = new Intent();
		bindIntent.setClass(getApplicationContext(), BackgroundService.class);
		bindService(bindIntent, sCon, Context.BIND_AUTO_CREATE);
		// <<<<<<<<<
//		mapView.drawPosition(Color.BLACK, 0, 1, 2, 3, 4);
	}

	private void initReceiver() {
		positionChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String Mac = intent.getStringExtra(BluetoothLe.EXTRA_MAC_ADDR);
				
			}
		};
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter iFilter = new IntentFilter(BluetoothLe.ACTION_NODE_DETECTED);
		registerReceiver(positionChangedReceiver, iFilter);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(positionChangedReceiver);
	}

	private void initViews() {
		btnLeader = (ToggleButton) findViewById(R.id.btnLeader);
		btnMember = (ToggleButton) findViewById(R.id.btnMember);
		btnSummon = (Button) findViewById(R.id.btnSummon);
		mapView = (MapView) findViewById(R.id.ivMap);
		btnLeader.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				final EditText teamNameInput = new EditText(getApplicationContext());
				teamNameInput.setTextColor(Color.BLACK);
				if (isChecked) {
					// ������������
					btnMember.setVisibility(View.INVISIBLE);
					btnSummon.setVisibility(View.VISIBLE);
					new AlertDialog.Builder(GroupActivity.this).setTitle("���������Ŷ����֣�")
							.setIcon(android.R.drawable.ic_dialog_info).setView(teamNameInput)
							.setPositiveButton("ȷ��", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String teamName = teamNameInput.getText().toString();
									if (checkInput(teamName)) {
										if (bBound) {
											serviceProxy.registTobeLeader(teamName);
										}
									} else {
										teamNameInput.setText("");
										Toast.makeText(getApplicationContext(), "�������ֲ��Ϸ�", Toast.LENGTH_SHORT).show();
										btnLeader.setChecked(false);
									}
									dialog.dismiss();
								}
							}).setNegativeButton("ȡ��", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									btnLeader.setChecked(false);
									dialog.dismiss();
								}
							}).show();
				} else {
					btnMember.setVisibility(View.VISIBLE);
					serviceProxy.unRegistLeader();
					btnSummon.setVisibility(View.INVISIBLE);
				}
			}
		});
		btnMember.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				final EditText teamNameInput = new EditText(getApplicationContext());
				teamNameInput.setTextColor(Color.BLACK);
				if (isChecked) {
					// ������������
					btnLeader.setVisibility(View.INVISIBLE);
					new AlertDialog.Builder(GroupActivity.this).setTitle("�������Ŷ����֣�")
							.setIcon(android.R.drawable.ic_dialog_info).setView(teamNameInput)
							.setPositiveButton("ȷ��", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String teamName = teamNameInput.getText().toString();
									if (checkInput(teamName)) {
										if (bBound) {
											serviceProxy.registTobeMember(teamName);
										}
									} else {
										teamNameInput.setText("");
										Toast.makeText(getApplicationContext(), "�������ֲ��Ϸ�", Toast.LENGTH_SHORT).show();
										btnMember.setChecked(false);
									}
									dialog.dismiss();
								}
							}).setNegativeButton("ȡ��", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									btnMember.setChecked(false);
									dialog.dismiss();
								}
							}).show();
				} else {
					btnLeader.setVisibility(View.VISIBLE);
					serviceProxy.unRegistMember();
				}
			}
		});
	}

	private boolean checkInput(String input) {
		return input.length() > 0 ? true : false;
	}
}