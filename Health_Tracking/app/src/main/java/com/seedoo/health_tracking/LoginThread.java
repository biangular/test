package com.seedoo.health_tracking;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;


class LoginThreadParams
{
    public String username;
    public String password;
    public String usercat;
    public String rc;
}

public class LoginThread extends Thread implements Runnable {

	public LoginThreadParams params;
	public LoginThread(Object parameters) {
		this.params = (LoginThreadParams) parameters;
	}

	@Override
	public void run() {
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("username", this.params.username));
			postParameters.add(new BasicNameValuePair("password",this.params.password));
			String response = null;
			try {
				response = SimpleHttpClient.executeHttpPost
						("http://www.yooyootoo.com/android/login.php",
						 postParameters);
				
				JSONObject jsonObject = new JSONObject(response);
				this.params.rc = jsonObject.getString("return_code");
				this.params.usercat = jsonObject.getString("usercat");
				
				//Toast.makeText(LoginActivity.this, "Congrats: " + strName + "/" + strPwd + " Login Successfull", Toast.LENGTH_LONG).show();
				//dialog.dismiss();			
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
