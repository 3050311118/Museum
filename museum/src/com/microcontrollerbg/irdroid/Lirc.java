package com.microcontrollerbg.irdroid;

import java.io.File;

public class Lirc {
	public static String POWER_TOGGLE = "Function18";
	static {
		System.loadLibrary("irdroid");
	}

	public native int parse(String filename);

	public native byte[] getIrBuffer(String irDevice, String irCode);

	public native String[] getDeviceList();

	native String[] getCommandList(String irDevice);

	public Lirc() {
		File dir = new File("/mnt/sdcard/log");
		dir.mkdirs();
	}
}
