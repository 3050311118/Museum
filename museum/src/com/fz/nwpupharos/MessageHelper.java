package com.fz.nwpupharos;

import org.apache.http.message.BasicNameValuePair;
import com.fz.nwpupharos.AsyncTaskUtils.HttpPostResultListener;

public class MessageHelper implements MConst {
	private static final String TAG = "@MessageHelper : ";

	public interface ResultListener {
		public void onResultSuccess();

		public void onResultFail(String error);
	}

	public static void sendPositionUpdate(PositionUpdate pu, final ResultListener resultListener) {
		BasicNameValuePair message_receiver = new BasicNameValuePair("message.receiver", pu.getDestinationMd5());
		BasicNameValuePair message_title = new BasicNameValuePair("message.title", pu.getTitle());// 此处存放Mac
		BasicNameValuePair content = new BasicNameValuePair("content", pu.getContent());
		BasicNameValuePair message_deadline = new BasicNameValuePair("message.deadline", "5/13/2018 09:49:43");
		BasicNameValuePair message_bluetooth_id = new BasicNameValuePair("message.bluetooth.id", String.valueOf(pu
				.getPositionId()));
		BasicNameValuePair message_type_id = new BasicNameValuePair("message.type.id", String.valueOf(3));
		AsyncTaskUtils.httpPost(ACTION_SNED_SMS, new HttpPostResultListener() {
			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				System.out.println(TAG + response);
			}

			@Override
			public void onError(String errorMsg) {
				// TODO Auto-generated method stub
				resultListener.onResultFail(errorMsg);// 发送失败
			}

			@Override
			public void onActionSuccess(String response) {
				// TODO Auto-generated method stub
				// 发送成功
				resultListener.onResultSuccess();
			}

			@Override
			public void onActionError(String response, String error) {
				// TODO Auto-generated method stub
				resultListener.onResultFail(error);// 发送失败
			}
		}, message_receiver, message_title, content, message_deadline, message_bluetooth_id, message_type_id);
	}

	public static void login() {
		AsyncTaskUtils.httpPost(ACTION_LOGIN, new HttpPostResultListener() {
			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				//	Logger.e(TAG, response);
			}

			@Override
			public void onError(String errorMsg) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onActionSuccess(String response) {
			}

			@Override
			public void onActionError(String response, String error) {
				// TODO Auto-generated method stub
				Logger.e(TAG, "loggin Error");
			}
		}, new BasicNameValuePair("businessname", BUSSINESS_NAME),
				new BasicNameValuePair("password", MD5.getMD5(PASSWORD)));
		Logger.e(TAG, MD5.getMD5(PASSWORD));
	}
}
