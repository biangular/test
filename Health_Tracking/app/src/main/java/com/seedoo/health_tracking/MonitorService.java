package com.seedoo.health_tracking;

import android.app.IntentService;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class MonitorService extends IntentService{
	private static final String TAG = "Monitor Service";
	private Boolean exitService;
	
	public MonitorService(){
		super(TAG);
		this.exitService = false;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.d(TAG, "onCreated called.");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.exitService = false;
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	protected void onHandleIntent(Intent intent){
		Log.d(TAG, "onStarted");
		sendSMS("9723754635", "onHandleIntent called!");
		while (this.exitService == false)
		{
		    // collect bluetooth data
		}
	}
	
	private void sendSMS(String phoneNumber, String message)
	{
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroyed called.");
	}
}
