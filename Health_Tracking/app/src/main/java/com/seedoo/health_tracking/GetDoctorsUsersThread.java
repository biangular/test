package com.seedoo.health_tracking;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class GetDoctorsUsersThreadParams
{
    public String doctor;
    public ArrayList<String> users;
    public String rc;
    
    public GetDoctorsUsersThreadParams()
    {
    	this.doctor = null;
    	this.users = new ArrayList<String>();
    	this.rc = "OK";
    }
}

public class GetDoctorsUsersThread extends Thread implements Runnable{
	private GetDoctorsUsersThreadParams params;

	public GetDoctorsUsersThread(Object parameters) {
		this.params = (GetDoctorsUsersThreadParams) parameters;
	}

	@Override
	public void run() {
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("doctor",this.params.doctor));
		String response = null;
		String rc = null;
		JSONObject jsonObject = null;
		try {
			response = SimpleHttpClient.executeHttpPost
					("http://www.yooyootoo.com/android/get_doctors_users.php",
					 postParameters);
			
			jsonObject = new JSONObject(response);
			rc = jsonObject.getString("return_code");		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.params.rc = rc;
		
		if ("OK".equals(rc))
		{
			try {
				JSONArray arr = jsonObject.getJSONArray("users");
				for (int i=0; i<arr.length(); i++)
				{
					this.params.users.add(arr.getString(i));
				}
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
