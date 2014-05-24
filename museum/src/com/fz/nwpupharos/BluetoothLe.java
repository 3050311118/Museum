package com.fz.nwpupharos;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

/**
 * @function 扫描蓝牙4.0LE信标，扫描获得结果时 广播节点Mac地址(String)：
 *           广播Intent action："com.fz.positionDetected"
 *           广播Intent内Mac域Name："com.fz.macAddr"
 * @useage:1、判断设备是否支持bluetooth 4.0 LE ：checkBLEsupport()，是，,执行2；
 *                             2、判断蓝牙是否打开：checkBLEAvailable()，是，执行3;
 *                             3、获取控制对象。
 * @author fz
 * 
 */
@SuppressLint({ "NewApi", "DefaultLocale" })
public class BluetoothLe {
	public static final String ACTION_NODE_DETECTED = "com.fz.bluetoothLe.action.positionDetected";
	public static final String EXTRA_MAC_ADDR = "com.fz.bluetoothLe.extra.macAddr";
	private static Context context;
	private static BluetoothLe monitor;
	private static Runnable continueScanTask = null;
	private LeScanCallback scanCallback = null;
	private Handler mHandler = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private static volatile boolean isScanning = false;
	private static ConcurrentHashMap<String, BluetoothNode> workingNodes = null;
	private static final String TAG = "@BluetoothLe : ";

	private BluetoothLe() {
		mHandler = new Handler(context.getMainLooper());
		BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		workingNodes = new ConcurrentHashMap<String, BluetoothNode>();
		scanCallback = new LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				if (isScanning) {
					// TODO Auto-generated method stub
					String macAddr = device.getAddress().replace(":", "").toLowerCase(Locale.US);
					BluetoothNode node = new BluetoothNode(macAddr, rssi);// 扫描获得的节点信息
					if (!workingNodes.containsKey(macAddr)) {
						// 发现新节点
						workingNodes.put(macAddr, node);
						// 广播上报发现的新节点
						Intent intent = new Intent(ACTION_NODE_DETECTED);
						intent.putExtra(EXTRA_MAC_ADDR, node.getMacAddr());
						context.sendBroadcast(intent);
					} else {
						// 节点已经存在，更新节点信息
//						System.out.println(TAG + macAddr + "existed");
						BluetoothNode nodeExisted = workingNodes.get(macAddr);
						nodeExisted.averRssi = (node.averRssi + nodeExisted.averRssi) / 2;
						nodeExisted.maxRssi = node.maxRssi > nodeExisted.maxRssi ? node.maxRssi : nodeExisted.maxRssi;
						nodeExisted.minRssi = node.minRssi < nodeExisted.minRssi ? node.maxRssi : nodeExisted.maxRssi;
						nodeExisted.detectable = true;// 更新节点活跃标志
					}
				}
			}
		};
	}

	/*
	 * 开始一次单位扫描前，经所有节点标识为不活跃，在扫描中更新状态
	 */
	private void setNodeDetectableFalse() {
		Set<Entry<String, BluetoothNode>> workingNodesSet = workingNodes.entrySet();
		for (Entry<String, BluetoothNode> e : workingNodesSet) {
			e.getValue().setDetectable(false);
		}
	}

	/*
	 * 结束一次单元扫描时，移除所有状态为不可发现的节点
	 */
	private void removeUnDetectableNode() {
		Set<Entry<String, BluetoothNode>> workingNodesSet = workingNodes.entrySet();
		for (Entry<String, BluetoothNode> e : workingNodesSet) {
			if (e.getValue().isDetectable() == false) {
				workingNodes.remove(e.getKey());
			}
		}
	}

	/**
	 * 
	 * @param context
	 * @return 如果设备支持bluetooth4.0
	 *         LE且蓝牙开启，则返回BLEMonitor实例，否则返回null。返回null的原因可能是手机不支持LE或者蓝牙未打开
	 *         ，通过该类方法查看。
	 */
	@SuppressLint("NewApi")
	public static BluetoothLe getInstance(Context context) {
		BluetoothLe.context = context;
		if (checkBLEAvailable()) {
			if (monitor == null) {
				System.out.println(TAG + "new monitor");
				return monitor = new BluetoothLe();
			} else {
				return monitor;
			}
		} else {
			return null;
		}
	}

	/**
	 * 开启蓝牙持续扫描：扫描+间隔+扫描+... +扫描+间隔+扫描-->节能考虑，持续扫描没有意义！
	 * 
	 * @param oneShotTime
	 *            单次扫描时间
	 * 
	 * @param interval
	 *            间隔
	 */
	public void startContinueScan(final int oneShotTime, final int interval) {
		if (isScanning) {
			// 如果当前正在扫描，则停止当前扫描，以便更改扫描时间参数
			if (continueScanTask != null) {
				stopScan();
			}
		}
		// 新建扫描任务，开始扫描
		isScanning = true;
		continueScanTask = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 开扫描时，将所有节点标志为不活跃
				setNodeDetectableFalse();
				mBluetoothAdapter.startLeScan(scanCallback);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mBluetoothAdapter.stopLeScan(scanCallback);
						// 停止扫描时移除不活跃节点
						removeUnDetectableNode();
					}
				}, interval);
				if (isScanning) {
					// 只有当正扫描标识为true时，才进行下一次单位扫描
					mHandler.postDelayed(this, oneShotTime + interval);
				} else {
					mHandler.removeCallbacks(this);
				}
			}
		};
		mHandler.post(continueScanTask);
	}

	/**
	 * 开启一次持续扫描时间为scanLastTime的扫描
	 * 
	 * @param scanLastTime
	 */
	public void startScanOnce(final int scanLastTime) {
		// TODO Auto-generated method stub
		// 开扫描时，将所有节点标志为不活跃
		setNodeDetectableFalse();
		mBluetoothAdapter.startLeScan(scanCallback);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mBluetoothAdapter.stopLeScan(scanCallback);
				// 停止扫描时移除不活跃节点
				removeUnDetectableNode();
			}
		}, scanLastTime);
	}

	/**
	 * 关闭蓝牙扫描
	 */
	public void stopScan() {
		if (isScanning) {
			isScanning = false;
			System.out.println(TAG + "stopScan()");
			if (continueScanTask != null) {
				System.out.println(TAG + "remove continueScan task!");
				mHandler.removeCallbacks(continueScanTask);
			}
			mBluetoothAdapter.stopLeScan(scanCallback);
		}
	}

	/**
	 * 
	 * @return 如果设备支持bluetooth4.0 LE 返回true否则返回false
	 */
	public static boolean checkBLESupport() {
		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @return 如果设备支持bluetooth4.0 LE ，且蓝牙已打开返回true否则返回false
	 */
	@SuppressLint("NewApi")
	public static boolean checkBLEAvailable() {
		if (checkBLESupport()) {
			final BluetoothManager bluetoothManager = (BluetoothManager) context
					.getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				return false;
			}
			if (mBluetoothAdapter.isEnabled()) {
				return true;
			}
			mBluetoothAdapter = null;
			return false;
		} else {
			return true;
		}
	}

	public static class BluetoothNode {
		private String macAddr;
		private int maxRssi, averRssi, minRssi;
		private boolean detectable = false;

		public BluetoothNode(String mac, int Rssi) {
			this.macAddr = mac;
			maxRssi = averRssi = minRssi = Rssi;
			detectable = true;
		}

		public boolean isDetectable() {
			return detectable;
		}

		public void setDetectable(boolean detectable) {
			this.detectable = detectable;
		}

		public int getMaxRssi() {
			return maxRssi;
		}

		public void setMaxRssi(int maxRssi) {
			this.maxRssi = maxRssi;
		}

		public int getAverRssi() {
			return averRssi;
		}

		public void setAverRssi(int averRssi) {
			this.averRssi = averRssi;
		}

		public int getMinRssi() {
			return minRssi;
		}

		public void setMinRssi(int minRssi) {
			this.minRssi = minRssi;
		}

		public String getMacAddr() {
			return macAddr;
		}

		public void setMacAddr(String macAddr) {
			this.macAddr = macAddr;
		}
	}
}
