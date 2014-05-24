package com.fz.nwpupharos;

import java.util.HashMap;
import java.util.Map;

public interface MConst {
	String BASE_DIR = "/storage/sdcard0/nwpupharos/pavilions/";
	String TTS_APPEND = ".txt";
	String HTML_APPEND = ".html";
	String[] pavilionsName = { "pavilion1", "pavilion2", "pavilion3", "pavilion4", "pavilion5" };
	String[] pavilionsTxtAbsDir = { BASE_DIR + "/" + pavilionsName[0] + "/" + pavilionsName[0] + TTS_APPEND,
			BASE_DIR + "/" + pavilionsName[1] + "/" + pavilionsName[1] + TTS_APPEND,
			BASE_DIR + "/" + pavilionsName[2] + "/" + pavilionsName[2] + TTS_APPEND,
			BASE_DIR + "/" + pavilionsName[3] + "/" + pavilionsName[3] + TTS_APPEND,
			BASE_DIR + "/" + pavilionsName[4] + "/" + pavilionsName[4] + TTS_APPEND };
	String[] pavilionsHtmlAbsDir = { BASE_DIR + "/" + pavilionsName[0] + "/" + pavilionsName[0] + HTML_APPEND,
			BASE_DIR + "/" + pavilionsName[1] + "/" + pavilionsName[1] + HTML_APPEND,
			BASE_DIR + "/" + pavilionsName[2] + "/" + pavilionsName[2] + HTML_APPEND,
			BASE_DIR + "/" + pavilionsName[3] + "/" + pavilionsName[3] + HTML_APPEND,
			BASE_DIR + "/" + pavilionsName[4] + "/" + pavilionsName[4] + HTML_APPEND };
	Map<String, String> MacAndInfo = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("20cd3981c697", pavilionsTxtAbsDir[0]);
			put("20cd3981c607", pavilionsTxtAbsDir[1]);
			put("20cd39806a0b", pavilionsTxtAbsDir[2]);
			put("20cd39807087", pavilionsTxtAbsDir[3]);
			put("20cd398070ab", pavilionsTxtAbsDir[4]);
		}
	};
	Map<String, Integer> MacAndIndex = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("20cd3981c697", 0);
			put("20cd3981c607", 1);
			put("20cd39806a0b", 2);
			put("20cd39807087", 3);
			put("20cd398070ab", 4);
		}
	};
	String SERVER_ADDR = "http://nwpupharos.duapp.com";
	String ACTION_LOGIN = SERVER_ADDR + "/public/business/LoginSubmit.action";
	String ACTION_SNED_SMS = SERVER_ADDR + "/private/message/MessageSaveAndCast.action";
	// fixed public user for making up a Group!
	String BUSSINESS_NAME = "ÖÜç÷";
	String PASSWORD = "111111";
	String passwordMd5 = "";
}
