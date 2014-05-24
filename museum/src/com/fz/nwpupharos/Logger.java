package com.fz.nwpupharos;

import android.util.Log;

public class Logger {
	private static boolean ifLog = true;

	public static void d(String tag, String msg) {
		if (ifLog) {
			Log.d(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (ifLog) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (ifLog) {
			Log.e(tag, msg);
		}
	}

	public static void enableLog() {
		ifLog = true;
	}

	public static void disableLog() {
		ifLog = false;
	}
}
