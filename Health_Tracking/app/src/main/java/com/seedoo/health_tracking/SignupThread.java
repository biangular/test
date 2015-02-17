package com.seedoo.health_tracking;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;


class SignupThreadParams
{
	public String email;
    public String username;
    public String password;
    public String rc;
    public String reason;
}

public class SignupThread extends Thread implements Runnable {

	public SignupThreadParams params;
	public SignupThread(Object parameters) {
		this.params = (SignupThreadParams) parameters;
	}

	@Override
	public void run() {
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("email", this.params.email));
			postParameters.add(new BasicNameValuePair("username", this.params.username));
			postParameters.add(new BasicNameValuePair("password",this.params.password));
			String response = null;
			try {
				response = SimpleHttpClient.executeHttpPost
						("http://www.yooyootoo.com/android/signup_new_user.php",
						 postParameters);
				JSONObject jsonObject = new JSONObject(response);
				this.params.rc = jsonObject.getString("return_code");	
				this.params.reason = jsonObject.getString("reason");
			} catch (Exception e) {
				this.params.rc = "FAIL";
				this.params.reason = e.getMessage();
				e.printStackTrace();
			}
	}
}
