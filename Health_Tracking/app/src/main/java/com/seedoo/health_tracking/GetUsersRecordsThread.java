package com.seedoo.health_tracking;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class UsersRecord
{
    String date;
    int    bp_sys;
    int    bp_dia;
    int    hb;
}

class GetUsersRecordsThreadParams
{
    public String user;
    public UsersRecord[] records;
    public String rc;
}

public class GetUsersRecordsThread extends Thread implements Runnable{
	private GetUsersRecordsThreadParams params;

	public GetUsersRecordsThread(Object parameters) {
		this.params = (GetUsersRecordsThreadParams) parameters;
	}

	@Override
	public void run() {
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("user",this.params.user));
		String response = null;
		String rc = null;
		JSONObject jsonObject = null;
		try {
			response = SimpleHttpClient.executeHttpPost
					("http://www.yooyootoo.com/android/get_users_records.php",
					 postParameters);
			
			jsonObject = new JSONObject(response);
			rc = jsonObject.getString("return_code");		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.params.rc = rc;
		this.params.records = null;
		
		if ("OK".equals(rc))
		{
			try {
				JSONArray arr = jsonObject.getJSONArray("UsersRecord");
				this.params.records = new UsersRecord[arr.length()];
				for (int i=0; i<arr.length(); i++)
				{
					// TODO: The following needs to be examined carefully
					JSONArray recArry = arr.getJSONArray(i);
					UsersRecord aRec = new UsersRecord();
					aRec.date = recArry.getString(0);
					aRec.bp_sys = recArry.getInt(1);
					aRec.bp_dia = recArry.getInt(2);
					aRec.hb = recArry.getInt(3);
					this.params.records[i] = aRec;
				}
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
	}
}
