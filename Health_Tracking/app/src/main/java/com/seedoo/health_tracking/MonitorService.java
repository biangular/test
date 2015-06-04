package com.seedoo.health_tracking;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.json.JSONObject;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MonitorService extends Service /*implements ConnectionCallbacks, OnConnectionFailedListener */ {
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

	private GoogleApiClient mGoogleApiClient;
	private BluetoothAdapter mBluetoothAdapter;
	private Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	private boolean mAllowInsecureConnections;
	private Context mContext;

	public static final int STATE_NONE = 0;			// we're doing nothing
	public static final int STATE_LISTEN = 1;		// now listening for incoming connections
	public static final int STATE_CONNECTING = 2; 	// now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;  	// now connected to a remote device

	public static BluetoothDevice device = null;
	public static String deviceName;
    public static String macAddress;

	private static final boolean D = true;
	private static final String TAG = "Monitor Service";
	private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String BT_DEVICE = "btdevice";



	private Boolean exitService;
	private HashSet<String> emailDests = new HashSet<String>();
	private HashSet<String> textDests = new HashSet<String>();

	private static Handler btHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 3:
					break;
				case 4:
					break;
				case 5:
					break;
				case -1:
					break;
				}
			}
			super.handleMessage(msg);
		}
	};

	public Handler getHandler() {
		return btHandler;
	}

    public static void setTargetDevice(String name) {
        BT_DEVICE = name;
    }

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this).
							//addConnectionCallbacks(this).
							//addOnConnectionFailedListener(this).
							addApi(LocationServices.API).
							build();
	}

	private void downloadAlarmDestinations(String deviceId)
	{
		// go to server to retrieve configured alarm destinations
		// using http request
		DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
		HttpPost httpPost = new HttpPost(Config.SERVER_URL+ "/htrack_managers.php?op_code=11&&device_id=" + deviceId);
		httpPost.setHeader("Content-type", "application/json");

		InputStream inputStream = null;
		String result = null;

		try
		{
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			inputStream = entity.getContent();
			BufferedReader reader = 
				new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
			result = sb.toString();
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				if (inputStream != null)
					inputStream.close();
			}
			catch(Exception squish)
			{
			}
		}

		try
		{
			JSONObject jObject = new JSONObject(result);
			Iterator<String> keys = jObject.keys();
			while(keys.hasNext())
			{
				String key = keys.next();
				int value = jObject.getInt(key);
				// 1 - text; 2 - email
				if(value == 1)
				{
					textDests.add(key);
				}
				else if (value == 2)
				{
					emailDests.add(key);
				}
			}
		}
		catch(org.json.JSONException e)
		{
			//
		}
	}

    private void uploadAlarm(String deviceMacAddress, long alarmTime )
    {
        // op_code=3: register fall detection alarm
        DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
        HttpPost httpPost = new HttpPost(Config.SERVER_URL+
                "/htrack_managers.php?op_code=3&&device_id=" + deviceMacAddress +
                "&&alarm_time=" + alarmTime);
        httpPost.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;

        try
        {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            inputStream = entity.getContent();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                if (inputStream != null)
                    inputStream.close();
            }
            catch(Exception squish)
            {
            }
        }

        try
        {
            JSONObject jObject = new JSONObject(result);
            String retCode = jObject.getString("rc");
            String alarm_id = "";
            if (retCode == "OK") {
                alarm_id = jObject.getString("alarm_id");
            }
        }
        catch(org.json.JSONException e)
        {
            //
        }
    }
	
	/*
	public MonitorService(){
		super(TAG);
		this.exitService = false;
		emailDests.clear();
		textDests.clear();
	}
	*/
	
	@Override
	public void onCreate(){
		Log.d(TAG, "onCreated called.");
		super.onCreate();
		buildGoogleApiClient();
	}

	@Override
	public IBinder onBind(Intent intent) {
		//mHandler = ((MonitorService) getApplication()).getHandler();
		return mBinder;
	}

	public class LocalBinder extends Binder {
		MonitorService getService() {
			return MonitorService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		this.exitService = false;
		//return super.onStartCommand(intent, flags, startId);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null ) {
			if (intent != null) {
				//device = (BluetoothDevice) intent.getSerializableExtra(BT_DEVICE);
                device = (BluetoothDevice) intent.getParcelableExtra(BT_DEVICE);
			}

			if (device != null) {
				deviceName = device.getName();
			    macAddress = device.getAddress();
				if (macAddress != null && macAddress.length() > 0) {
					connectToDevice(macAddress);
				}
				else {
					stopSelf();
					return 0;
				}
                downloadAlarmDestinations(macAddress);
			}
		}

		if (intent != null) {
			String stopservice = intent.getStringExtra("stopservice");
			if (stopservice != null && stopservice.length() > 0) {
				stop();
			}
		}

		return START_STICKY;
	}

	private synchronized void connectToDevice(String macAddr) {
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddr);
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	private void setState(int state) {
		/*MonitorService.*/mState = state;
		if (mHandler != null) {
			mHandler.obtainMessage(MonitorService.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
		}
	}

	public synchronized void stop() {
		setState(STATE_NONE);
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		stopSelf();
	}

	@Override
	public boolean stopService(Intent name) {
		setState(STATE_NONE);
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		return super.stopService(name);
	}

	private void connectionFailed() {
		MonitorService.this.stop();
		Message msg = mHandler.obtainMessage(MonitorService.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString("TOAST", getString(R.string.error_connect_failed));
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	private void connectionLost() {
		MonitorService.this.stop();
		Message msg = mHandler.obtainMessage(MonitorService.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString("TOAST", getString(R.string.error_connect_lost));
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}


	private static Object obj = new Object();

	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (obj) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	private synchronized void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
	
		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
	
		mConnectedThread = new ConnectedThread(mmSocket);
		mConnectedThread.start();
	
		// Message msg =
		// mHandler.obtainMessage(AbstractActivity.MESSAGE_DEVICE_NAME);
		// Bundle bundle = new Bundle();
		// bundle.putString(AbstractActivity.DEVICE_NAME, "p25");
		// msg.setData(bundle);
		// mHandler.sendMessage(msg);
		setState(STATE_CONNECTED);
	}
	
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
	
		public ConnectThread(BluetoothDevice device) {
			this.mmDevice = device;
			BluetoothSocket tmp = null;
			try {
				tmp = device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmSocket = tmp;
		}
	
		@Override
		public void run() {
			setName("ConnectThread");
			mBluetoothAdapter.cancelDiscovery();
			try {
				mmSocket.connect();
			} catch (IOException e) {
				try {
					mmSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				connectionFailed();
				return;
	
			}
			synchronized (MonitorService.this) {
				mConnectThread = null;
			}
			connected(mmSocket, mmDevice);
		}
	
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
	
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
	
		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
	
		@Override
		public void run() {
			int nBytes;
			byte[] buffer = new byte[1024];

			SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String timeNow;
            long   timeNowLong;

			while (exitService == false) {
				try {
					nBytes = mmInStream.read(buffer);
					if (nBytes > 0) {
                        Date dateNow = new Date();
						timeNow = timeFormat.format(dateNow);
                        timeNowLong = dateNow.getTime()/1000;
						String longitude = "Unknown";
						String latitude = "Unknown";
						Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
						if (lastLocation != null)
						{
							longitude = String.valueOf(lastLocation.getLongitude());
							latitude = String.valueOf(lastLocation.getLatitude());
						}
						Byte rawByte = buffer[0];
						boolean isAlarm = ((rawByte >> 4) == 1);
						boolean needAck = ((rawByte & 1) == 1);
						if (isAlarm) {
							String msg = timeNow + ",LON " + longitude + ",LAT " + latitude + ",FALL DETECTED.";
							
							// send text
							if (!textDests.isEmpty()) {
								Iterator<String> iter = textDests.iterator();
								while (iter.hasNext()) {
									sendSMS(iter.next(), msg);
								}
							}

							// send email
							//private HashSet emailDests = nll;
							//sendmail tutorial
							//http://www.jondev.net/articles/Sending_Emails_without_User_Intervention_(no_Intents)_in_Android
							//need to download http://code.google.com/p/javamail-android/downloads/list and add it to "external libraries"
							//they are placed in Health_Tracking/app/libs directory, and were added as libraries (see project build.gradle file)
							if (!emailDests.isEmpty()) {
                                Mail m= new Mail();
                                ArrayList<String> toList = new ArrayList<String>();
                                Iterator<String> iter = emailDests.iterator();
                                while (iter.hasNext()) {
                                    toList.add(iter.next());
                                }
                                String[] toArr = new String[toList.size()];
                                toArr = toList.toArray(toArr);
                                m.setTo(toArr);
                                m.setFrom("okaybian@gmail.com");
                                m.setSubject("Fall Detection Alarm");
                                m.setBody(msg);
                                try {
                                    if (m.send()) {

                                    }
                                    else {

                                    }
                                }
                                catch(Exception e) {
                                    //
                                }
                            }

                            // upload the alarm to web server for recording
                            uploadAlarm(macAddress,timeNowLong);
							
							// ack if needed
							if (needAck) {
                                // high order 4 bit value 2 is ack
                                // send back a byte [0010 0000]
                                byte[] ack = new byte[1];
                                ack[0] = (byte) 32;
                                mmOutStream.write(ack);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					connectionLost();
					MonitorService.this.stop();
					break;
				}
			}
		}
	
		private byte[] btBuff;
	
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
	
				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(MonitorService.MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}
	
		public void cancel() {
			try {
				mmSocket.close();
	
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	
	}
	
	public void trace(String msg) {
		Log.d(TAG, msg);
		toast(msg);
	}
	
	public void toast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onDestroy() {
		stop();
		Log.d(TAG, "Destroyed");
		super.onDestroy();
	}
	
	private void sendMsg(int flag) {
		Message msg = new Message();
		msg.what = flag;
		mHandler.sendMessage(msg);
	}



    /*
	@Override
	protected void onHandleIntent(Intent intent){
		Log.d(TAG, "onStarted");
		sendSMS("9723754635", "onHandleIntent called!");
		while (this.exitService == false)
		{
			// collect bluetooth data
		}
	}
    */
	
	private void sendSMS(String phoneNumber, String message)
	{
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}
}
