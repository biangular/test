package com.seedoo.health_tracking;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class LogoutThread extends Thread implements Runnable{

	public LogoutThread(Object parameters) {
	}

	@Override
	public void run() {
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("username", MainActivity.logged_in_user));
		String response = null;
		String rc = null;
		try {
			response = SimpleHttpClient.executeHttpPost
					("http://www.yooyootoo.com/android/logout.php",
					 postParameters);
			
			JSONObject jsonObject = new JSONObject(response);
			rc = jsonObject.getString("return_code");		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if ("OK".equals(rc))
		{
			MainActivity.logged_in_user = null;
		}
	}
}
