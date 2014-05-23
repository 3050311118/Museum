package com.fz.nwpupharos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

public class AsyncTaskUtils {
	public static ExecutorService executorService = Executors.newFixedThreadPool(5);// �����̳߳�ȫ�ֵ�
	private static String SESSION_ID = "";// �ͻ���ά��Session ID

	public interface HttpPostResultListener {
		public void onResponse(String response);

		public void onError(String errorMsg);

		public void onActionSuccess(String response);

		public void onActionError(String response, String error);
	}

	/**
	 * 
	 * @param url
	 *            url to post.
	 * @param httpPostResultListener
	 *            post result listener.
	 * @param keyValue
	 *            post parameters.
	 */
	public static void httpPost(final String url, final HttpPostResultListener httpPostResultListener,
			final BasicNameValuePair... keyValue) {
		BasicHttpParams httpParameters = new BasicHttpParams();
		// ���ý������ӳ�ʱ
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		// ����Socket�ȴ���������ʱ�䳬ʱ
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		final DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<BasicNameValuePair> parameters = Arrays.asList(keyValue);
				// System.out.println("Task run!");
				HttpPost postMethod = new HttpPost(url);
				if (SESSION_ID != "") {
					postMethod.setHeader("Cookie", "JSESSIONID=" + SESSION_ID);
					Logger.w("AsyncTaskUtils", SESSION_ID);
				}
				try {
					postMethod.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					httpClient.execute(postMethod, new ResponseHandler<String>() {
						@Override
						public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								HttpEntity entity = (HttpEntity) response.getEntity();
								if (entity != null) {
									InputStream input = entity.getContent();
									// ----------------
									CookieStore mCookieStore = httpClient.getCookieStore();
									List<Cookie> cookies = mCookieStore.getCookies();
									// ���»���SESSIONID
									for (int i = 0; i < cookies.size(); i++) {
										// �����Ƕ�ȡCookie['BAEID']��ֵ���ھ�̬�����У���֤ÿ�ζ���ͬһ��ֵ
										if ("JSESSIONID".equals(cookies.get(i).getName())) {
											SESSION_ID = cookies.get(i).getValue();
											break;
										}
									}
									// ----------------
									BufferedReader resultReader = new BufferedReader(new InputStreamReader(input));
									char[] resultBuffer = new char[1024];
									StringBuilder result = new StringBuilder();
									int count = 0;
									while ((count = resultReader.read(resultBuffer)) > 0) {
										result.append(resultBuffer, 0, count);
									}
									input.close();
									if (httpPostResultListener != null) {
										String resultStr = result.toString();
										httpPostResultListener.onResponse(resultStr);
										JSONObject resultObj;
										// �������actioMessages��Action(��֪ͨ���������������Լ�������֪ͨʧ�ܵķ���)
										try {
											resultObj = new JSONObject(resultStr);
											String actionMessage = (String) resultObj.getJSONArray("actionMessages")
													.get(0);
											if (actionMessage.equals("success")) {
												httpPostResultListener.onActionSuccess(resultStr);
											} else {
												String error = (String) resultObj.getJSONArray("actionErrors").get(0);
												httpPostResultListener.onActionError(resultStr, error);
											}
										} catch (JSONException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
											// ���صĲ��Ǳ�׼JSON���Ǵ���
											httpPostResultListener.onError("�������쳣��");
										}
									}
								}
							} else {
								if (httpPostResultListener != null) {
									httpPostResultListener.onError("Server Exception!" + "ERROR_CODE:"
											+ response.getStatusLine().getStatusCode());
								}
							}
							return null;
						}
					});
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					if (httpPostResultListener != null) {
						httpPostResultListener.onError(e.getMessage());
					}
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
					if (httpPostResultListener != null) {
						httpPostResultListener.onError(e.getMessage());
					}
				}
			}
		});
	}

	public static void asyncDo(Runnable job) {
		executorService.execute(job);
	}
}
