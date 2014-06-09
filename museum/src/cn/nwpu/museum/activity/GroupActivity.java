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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GroupActivity extends Activity implements MConst {
	private ToggleButton btnLeader, btnMember;
	private Button btnSummon;
	private static final String TAG = "GroupActivity";
	// Actually in the same process,this proxy is the real service`s reference!
	private BackgroundService serviceProxy;
	private boolean bBound = false;
	private ServiceConnection sCon;
	private MapView mapView;
	private TextView tvTeamName;
	private BroadcastReceiver positionChangedReceiver;
	// 组长位置更新广播接受者
	private BroadcastReceiver leaderPositionChangeReceiver;
	private SharedPreferences usePre;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		usePre = getSharedPreferences("userPre", Context.MODE_PRIVATE);
		setContentView(R.layout.activity_group);
		initReceiver();
		initViews();
		initLeaderPosChangeReceiver();
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
				serviceProxy.stopSpeek();
				bBound = true;
				Logger.w(TAG, "bind service success");
			}
		};
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 绑定backgroundService
		Intent bindIntent = new Intent();
		bindIntent.setClass(getApplicationContext(), BackgroundService.class);
		bindService(bindIntent, sCon, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(positionChangedReceiver);
		unregisterReceiver(leaderPositionChangeReceiver);
		if (bBound) {
			bBound = false;
			unbindService(sCon);
		}
		usePre.edit().putBoolean("autoSpeek", true).commit();// 打开自动播报
	}

	private void initReceiver() {
		positionChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String Mac = intent.getStringExtra(BluetoothLe.EXTRA_MAC_ADDR);
				int index = MacAndIndex.get(Mac);
				mapView.drawMyself(index);
				usePre.edit().putInt("myPosition", index).commit();
			}
		};
	}

	private void initLeaderPosChangeReceiver() {
		leaderPositionChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Logger.e(TAG, "leader position update, draw position");
				String leaderMac = intent.getStringExtra(BackgroundService.EXTRA_MAC_ADD);
				if (leaderMac.equals("summon")) {
					// 接收到领队召集信息.显示提示
					Logger.d(TAG, "领队召集");
					AlertDialog dialog = new AlertDialog.Builder(GroupActivity.this).setIcon(R.drawable.ic_launcher)
							.setTitle("集合").setMessage("请注意：领队召集大家集合！").setNegativeButton("取消", null)
							.setPositiveButton("确定", null).create();
					dialog.show();
				} else {
					if (leaderMac != null) {
//					mapView.drawLeader(MacAndIndex.get(leaderMac));
						mapView.drawMeAndLeader(usePre.getInt("myPosition", -1), MacAndIndex.get(leaderMac));
						usePre.edit().putInt("leaderPosition", MacAndIndex.get(leaderMac));
					}
				}
			}
		};
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// -----------
		IntentFilter iFilter = new IntentFilter(BluetoothLe.ACTION_NODE_DETECTED);
		registerReceiver(positionChangedReceiver, iFilter);
		IntentFilter iFilter1 = new IntentFilter(BackgroundService.ACTION_LEADER_POSITION_UPDATE);
		registerReceiver(leaderPositionChangeReceiver, iFilter1);
		restorViews();
		usePre.edit().putBoolean("autoSpeek", false).commit();// 关闭自动播报
	}

	private void initViews() {
		btnLeader = (ToggleButton) findViewById(R.id.btnLeader);
		btnLeader.setChecked(usePre.getBoolean("isLeader", false));
		btnMember = (ToggleButton) findViewById(R.id.btnMember);
		btnMember.setChecked(usePre.getBoolean("isMember", false));
		btnSummon = (Button) findViewById(R.id.btnSummon);
		btnSummon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (bBound) {
					serviceProxy.leaderUpdatePosition(usePre.getString("teamName", null) + ":" + "summon");// 特殊Mac：summon，表示领队召集信息
				}
			}
		});
		mapView = (MapView) findViewById(R.id.ivMap);
		tvTeamName = (TextView) findViewById(R.id.tvTeamName);
		btnLeader.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				final EditText teamNameInput = new EditText(getApplicationContext());
				teamNameInput.setTextColor(Color.BLACK);
				if (isChecked) {
					// 创建队伍流程
					btnMember.setVisibility(View.INVISIBLE);
					btnSummon.setVisibility(View.VISIBLE);
					new AlertDialog.Builder(GroupActivity.this).setTitle("请输入新团队名字：")
							.setIcon(android.R.drawable.ic_dialog_info).setView(teamNameInput)
							.setPositiveButton("确定", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String teamName = teamNameInput.getText().toString();
									if (checkInput(teamName)) {
										if (bBound) {
											serviceProxy.registTobeLeader(teamName);
										}
										tvTeamName.setText(teamName);
										tvTeamName.setVisibility(View.VISIBLE);
									} else {
										teamNameInput.setText("");
										Toast.makeText(getApplicationContext(), "队伍名字不合法", Toast.LENGTH_SHORT).show();
										btnLeader.setChecked(false);
									}
									dialog.dismiss();
								}
							}).setNegativeButton("取消", new OnClickListener() {
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
					tvTeamName.setVisibility(View.INVISIBLE);
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
					// 创建队伍流程
					btnLeader.setVisibility(View.INVISIBLE);
					new AlertDialog.Builder(GroupActivity.this).setTitle("请输入团队名字：")
							.setIcon(android.R.drawable.ic_dialog_info).setView(teamNameInput)
							.setPositiveButton("确定", new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String teamName = teamNameInput.getText().toString();
									if (checkInput(teamName)) {
										if (bBound) {
											serviceProxy.registTobeMember(teamName);
											tvTeamName.setText(teamName);
											tvTeamName.setVisibility(View.VISIBLE);
										}
									} else {
										teamNameInput.setText("");
										Toast.makeText(getApplicationContext(), "队伍名字不合法", Toast.LENGTH_SHORT).show();
										btnMember.setChecked(false);
									}
									dialog.dismiss();
								}
							}).setNegativeButton("取消", new OnClickListener() {
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
					tvTeamName.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	private void restorViews() {
		if (usePre.getBoolean("isLeader", false)) {
			btnLeader.setChecked(true);
			btnSummon.setVisibility(View.VISIBLE);
			btnMember.setVisibility(View.INVISIBLE);
			mapView.drawMeAndLeader(usePre.getInt("myPosition", -1), usePre.getInt("leaderPosition", -1));
			tvTeamName.setText(usePre.getString("teamName", null));
			tvTeamName.setVisibility(View.VISIBLE);
		}
		if (usePre.getBoolean("isMember", false)) {
			btnLeader.setVisibility(View.INVISIBLE);
			btnSummon.setVisibility(View.INVISIBLE);
			btnMember.setChecked(true);
			mapView.drawMyself(usePre.getInt("myPosition", -1));
			tvTeamName.setText(usePre.getString("teamName", null));
			tvTeamName.setVisibility(View.VISIBLE);
		}
	}

	private boolean checkInput(String input) {
		return input.length() > 0 ? true : false;
	}
}
