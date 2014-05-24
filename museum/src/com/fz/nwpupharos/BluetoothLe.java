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
 * @function ɨ������4.0LE�ű꣬ɨ���ý��ʱ �㲥�ڵ�Mac��ַ(String)��
 *           �㲥Intent action��"com.fz.positionDetected"
 *           �㲥Intent��Mac��Name��"com.fz.macAddr"
 * @useage:1���ж��豸�Ƿ�֧��bluetooth 4.0 LE ��checkBLEsupport()���ǣ�,ִ��2��
 *                             2���ж������Ƿ�򿪣�checkBLEAvailable()���ǣ�ִ��3;
 *                             3����ȡ���ƶ���
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
					BluetoothNode node = new BluetoothNode(macAddr, rssi);// ɨ���õĽڵ���Ϣ
					if (!workingNodes.containsKey(macAddr)) {
						// �����½ڵ�
						workingNodes.put(macAddr, node);
						// �㲥�ϱ����ֵ��½ڵ�
						Intent intent = new Intent(ACTION_NODE_DETECTED);
						intent.putExtra(EXTRA_MAC_ADDR, node.getMacAddr());
						context.sendBroadcast(intent);
					} else {
						// �ڵ��Ѿ����ڣ����½ڵ���Ϣ
//						System.out.println(TAG + macAddr + "existed");
						BluetoothNode nodeExisted = workingNodes.get(macAddr);
						nodeExisted.averRssi = (node.averRssi + nodeExisted.averRssi) / 2;
						nodeExisted.maxRssi = node.maxRssi > nodeExisted.maxRssi ? node.maxRssi : nodeExisted.maxRssi;
						nodeExisted.minRssi = node.minRssi < nodeExisted.minRssi ? node.maxRssi : nodeExisted.maxRssi;
						nodeExisted.detectable = true;// ���½ڵ��Ծ��־
					}
				}
			}
		};
	}

	/*
	 * ��ʼһ�ε�λɨ��ǰ�������нڵ��ʶΪ����Ծ����ɨ���и���״̬
	 */
	private void setNodeDetectableFalse() {
		Set<Entry<String, BluetoothNode>> workingNodesSet = workingNodes.entrySet();
		for (Entry<String, BluetoothNode> e : workingNodesSet) {
			e.getValue().setDetectable(false);
		}
	}

	/*
	 * ����һ�ε�Ԫɨ��ʱ���Ƴ�����״̬Ϊ���ɷ��ֵĽڵ�
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
	 * @return ����豸֧��bluetooth4.0
	 *         LE�������������򷵻�BLEMonitorʵ�������򷵻�null������null��ԭ��������ֻ���֧��LE��������δ��
	 *         ��ͨ�����෽���鿴��
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
	 * ������������ɨ�裺ɨ��+���+ɨ��+... +ɨ��+���+ɨ��-->���ܿ��ǣ�����ɨ��û�����壡
	 * 
	 * @param oneShotTime
	 *            ����ɨ��ʱ��
	 * 
	 * @param interval
	 *            ���
	 */
	public void startContinueScan(final int oneShotTime, final int interval) {
		if (isScanning) {
			// �����ǰ����ɨ�裬��ֹͣ��ǰɨ�裬�Ա����ɨ��ʱ�����
			if (continueScanTask != null) {
				stopScan();
			}
		}
		// �½�ɨ�����񣬿�ʼɨ��
		isScanning = true;
		continueScanTask = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// ��ɨ��ʱ�������нڵ��־Ϊ����Ծ
				setNodeDetectableFalse();
				mBluetoothAdapter.startLeScan(scanCallback);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mBluetoothAdapter.stopLeScan(scanCallback);
						// ֹͣɨ��ʱ�Ƴ�����Ծ�ڵ�
						removeUnDetectableNode();
					}
				}, interval);
				if (isScanning) {
					// ֻ�е���ɨ���ʶΪtrueʱ���Ž�����һ�ε�λɨ��
					mHandler.postDelayed(this, oneShotTime + interval);
				} else {
					mHandler.removeCallbacks(this);
				}
			}
		};
		mHandler.post(continueScanTask);
	}

	/**
	 * ����һ�γ���ɨ��ʱ��ΪscanLastTime��ɨ��
	 * 
	 * @param scanLastTime
	 */
	public void startScanOnce(final int scanLastTime) {
		// TODO Auto-generated method stub
		// ��ɨ��ʱ�������нڵ��־Ϊ����Ծ
		setNodeDetectableFalse();
		mBluetoothAdapter.startLeScan(scanCallback);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mBluetoothAdapter.stopLeScan(scanCallback);
				// ֹͣɨ��ʱ�Ƴ�����Ծ�ڵ�
				removeUnDetectableNode();
			}
		}, scanLastTime);
	}

	/**
	 * �ر�����ɨ��
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
	 * @return ����豸֧��bluetooth4.0 LE ����true���򷵻�false
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
	 * @return ����豸֧��bluetooth4.0 LE ���������Ѵ򿪷���true���򷵻�false
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
