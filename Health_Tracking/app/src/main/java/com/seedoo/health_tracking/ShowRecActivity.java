package com.seedoo.health_tracking;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ShowRecActivity extends ActionBarActivity 
							 implements OnClickListener, OnItemSelectedListener {

	GetUsersRecordsThreadParams threadParams;
	private Button btnLogout;
	
	@Override
	public void onItemSelected(AdapterView<?> a, View v, int pos, long id)
	{
		switch(a.getId()) 
		{
		case R.id.spinnerUserName:
			getAndShowRecords();
			break;
		case R.id.spinnerStartDate:
			startDateChanged(pos);
			break;
		case R.id.spinnerEndDate:
			endDateChanged(pos);
			break;
		default:
			break;
		}
		
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> a)
	{
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_rec);
		
		btnLogout = (Button) findViewById(R.id.btnLogout);
		btnLogout.setOnClickListener(this);

		Spinner startDate = (Spinner) findViewById(R.id.spinnerStartDate);
		Spinner endDate = (Spinner) findViewById(R.id.spinnerEndDate);
		startDate.setOnItemSelectedListener(this);
		endDate.setOnItemSelectedListener(this);
		
		Spinner user = (Spinner) findViewById(R.id.spinnerUserName);
		user.setOnItemSelectedListener(this);
		
		populateUserLists();
		getAndShowRecords();
	}

	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnLogout:
			threadParams.records = null;
			startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			finish();
			break;
		case R.id.spinnerStartDate:
			break;
		case R.id.spinnerEndDate:
			break;
		}       	
    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_show_rec, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_close:
			threadParams.records = null;
			finish();
			startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			//finish();
		    return true;
		case R.id.action_email_me:
			// TODO
		    return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/*///////////////////////////////////
	@Override
	public void onStop()
	{
		super.onStop();
		LogoutThread logoutThread = new LogoutThread(null);
		try {
			logoutThread.start();
			logoutThread.join();
    	} 
        catch (Exception e) 
    	{
    		e.printStackTrace();
    	}
		this.threadParams.records = null;
	}
	*/////////////////////////////////////
	
	@Override
    protected void onDestroy() {
	    super.onDestroy();
	 
    }
	
	public void getAndShowRecords()
	{
		// get Recores from database
		// MainActivity.logged_in_user and MainActivity.logged_in_user_cat
		// get start and end date
		// When first loaded, try to retrieve the last month's data and display them
		// later when the user change the dates, get new data if necessary
		// make sure endDate is after startDate
		// if current data has the interval (startDate and endDate) covered
		// do not go to the server to retrieve new data, just show existing data
		
		Spinner user = (Spinner) findViewById(R.id.spinnerUserName);
		Spinner startDate = (Spinner) findViewById(R.id.spinnerStartDate);
		Spinner endDate = (Spinner) findViewById(R.id.spinnerEndDate);
		
		String  selectedUser = user.getSelectedItem().toString();
		
		// get records
		this.threadParams = new GetUsersRecordsThreadParams();
		threadParams.user = selectedUser;
		
		GetUsersRecordsThread thr = new GetUsersRecordsThread(threadParams);
		try {
			thr.start();
			thr.join();
			if ("OK".equals(threadParams.rc) == false)
			{
				// TODO: clear data
				return;
			}
		}
		catch(Exception e) 
    	{
    		e.printStackTrace();
    		// TODO: clear data
    		return;
    	}
		
		// Fill in start date and end date
		// returned data are ordered by date in ascending order, in threadParams.records
		ArrayList<String> datesList = new ArrayList<String>();
		for (int i = 0; i < threadParams.records.length; i++) {
			datesList.add(threadParams.records[i].date);
		}
		ArrayAdapter<String> da = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, datesList);
		da.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		startDate.setAdapter(da);
		startDate.setSelection(0);	
		endDate.setAdapter(da);
		endDate.setSelection(threadParams.records.length-1);
		
		// show records
		TableLayout ll = (TableLayout) findViewById(R.id.tableRecords);
		
		// clear out anything left-over
		int count = ll.getChildCount();
		for (int i=0; i < count; i++) {
			View child = ll.getChildAt(i);
			if (child instanceof TableRow) ((ViewGroup)child).removeAllViews();
		}
		
		


		TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
		lp.setMargins(1, 1, 1, 1);
		
		TableRow headerRow= new TableRow(this);
        headerRow.setLayoutParams(lp);
        TextView tvTimeHeader = new TextView(this);
        tvTimeHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvTimeHeader.setLayoutParams(lp);
        tvTimeHeader.setBackgroundResource(R.color.SkyBlue);
        tvTimeHeader.setText(getString(R.string.record_time));
        
        TextView tvBPSysHeader = new TextView(this);
        tvBPSysHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvBPSysHeader.setLayoutParams(lp);
        tvBPSysHeader.setBackgroundResource(R.color.SkyBlue);
        tvBPSysHeader.setText(getString(R.string.systolic));
        
        TextView tvBPDiaHeader = new TextView(this);
        tvBPDiaHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvBPDiaHeader.setLayoutParams(lp);
        tvBPDiaHeader.setBackgroundResource(R.color.SkyBlue);
        tvBPDiaHeader.setText(getString(R.string.diastolic));
        
        TextView tvHBHeader = new TextView(this);
        tvHBHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvHBHeader.setLayoutParams(lp);
        tvHBHeader.setBackgroundResource(R.color.SkyBlue);
        tvHBHeader.setText(getString(R.string.heartbeat));
        
        headerRow.addView(tvTimeHeader);
        headerRow.addView(tvBPSysHeader);
        headerRow.addView(tvBPDiaHeader);
        headerRow.addView(tvHBHeader);
        ll.addView(headerRow, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        
	    for (int i = 0; i < threadParams.records.length; i++) {
	        TableRow row= new TableRow(this);
	        row.setLayoutParams(lp);
	        
	        TextView tvTime = new TextView(this);
	        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvTime.setLayoutParams(lp);
	        tvTimeHeader.setBackgroundResource(R.color.SkyBlue);
	        tvTime.setText(threadParams.records[i].date);
	        
	        TextView tvBPSys = new TextView(this);
	        tvBPSys.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvBPSys.setLayoutParams(lp);
	        tvBPSys.setBackgroundResource(R.color.SkyBlue);
	        tvBPSys.setText(Integer.toString(threadParams.records[i].bp_sys));
	        
	        TextView tvBPDia = new TextView(this);
	        tvBPDia.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvBPDia.setLayoutParams(lp);
	        tvBPDia.setBackgroundResource(R.color.SkyBlue);
	        tvBPDia.setText(Integer.toString(threadParams.records[i].bp_dia));
	        
	        TextView tvHB = new TextView(this);
	        tvHB.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvHB.setLayoutParams(lp);
	        tvHB.setBackgroundResource(R.color.SkyBlue);
	        tvHB.setText(Integer.toString(threadParams.records[i].hb));
	        
	        row.addView(tvTime);
	        row.addView(tvBPSys);
	        row.addView(tvBPDia);
	        row.addView(tvHB);
	        ll.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
	    }  
	}
	
	private void populateUserLists()
	{
		ArrayList<String> arrayUsers = new ArrayList<String>();
		GetDoctorsUsersThreadParams threadParams = new GetDoctorsUsersThreadParams();
		Spinner user = (Spinner) findViewById(R.id.spinnerUserName);
		if (MainActivity.logged_in_user == null)
		{
			arrayUsers.clear();
		}
		else if (MainActivity.logged_in_user_cat != null && MainActivity.logged_in_user_cat.equals("doctor"))
		{
			threadParams.doctor = MainActivity.logged_in_user;
			GetDoctorsUsersThread theThread = new GetDoctorsUsersThread(threadParams);
			try {
				theThread.start();
				theThread.join();
				if ("OK".equals(threadParams.rc))
				{
					arrayUsers = threadParams.users;
				}
			}
			catch(Exception e) 
	    	{
	    		e.printStackTrace();
	    	}       
		}
		else
		{
			arrayUsers.add(MainActivity.logged_in_user);
		}
		
		// populate the spinner
		// populate ...
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayUsers);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		user.setAdapter(aa);
		user.setSelection(0);
		return;
	}
	
	public void showRecords(int startPos, int endPos)
	{
		TableLayout ll = (TableLayout) findViewById(R.id.tableRecords);
		
		// clear out anything left-over
		int count = ll.getChildCount();
		for (int i=0; i < count; i++) {
			View child = ll.getChildAt(i);
			if (child instanceof TableRow) ((ViewGroup)child).removeAllViews();
		}
		
		

		TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
		lp.setMargins(1, 1, 1, 1);
		
		TableRow headerRow= new TableRow(this);
        headerRow.setLayoutParams(lp);
        TextView tvTimeHeader = new TextView(this);
        tvTimeHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvTimeHeader.setLayoutParams(lp);
        tvTimeHeader.setBackgroundResource(R.color.SkyBlue);
        tvTimeHeader.setText(getString(R.string.record_time));
        
        TextView tvBPSysHeader = new TextView(this);
        tvBPSysHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvBPSysHeader.setLayoutParams(lp);
        tvBPSysHeader.setBackgroundResource(R.color.SkyBlue);
        tvBPSysHeader.setText(getString(R.string.systolic));
        
        TextView tvBPDiaHeader = new TextView(this);
        tvBPDiaHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvBPDiaHeader.setLayoutParams(lp);
        tvBPDiaHeader.setBackgroundResource(R.color.SkyBlue);
        tvBPDiaHeader.setText(getString(R.string.diastolic));
        
        TextView tvHBHeader = new TextView(this);
        tvHBHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        tvHBHeader.setLayoutParams(lp);
        tvHBHeader.setBackgroundResource(R.color.SkyBlue);
        tvHBHeader.setText(getString(R.string.heartbeat));
        
        headerRow.addView(tvTimeHeader);
        headerRow.addView(tvBPSysHeader);
        headerRow.addView(tvBPDiaHeader);
        headerRow.addView(tvHBHeader);
        ll.addView(headerRow, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        
	    for (int i = startPos; i <= endPos; i++) {
	        TableRow row= new TableRow(this);
	        row.setLayoutParams(lp);
	        
	        TextView tvTime = new TextView(this);
	        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvTime.setLayoutParams(lp);
	        tvTime.setBackgroundResource(R.color.SkyBlue);
	        tvTime.setText(threadParams.records[i].date);
	        
	        TextView tvBPSys = new TextView(this);
	        tvBPSys.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvBPSys.setLayoutParams(lp);
	        tvBPSys.setBackgroundResource(R.color.SkyBlue);
	        tvBPSys.setText(Integer.toString(threadParams.records[i].bp_sys));
	        
	        TextView tvBPDia = new TextView(this);
	        tvBPDia.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvBPDia.setLayoutParams(lp);
	        tvBPDia.setBackgroundResource(R.color.SkyBlue);
	        tvBPDia.setText(Integer.toString(threadParams.records[i].bp_dia));
	        
	        TextView tvHB = new TextView(this);
	        tvHB.setGravity(Gravity.CENTER_HORIZONTAL);
	        tvHB.setLayoutParams(lp);
	        tvHB.setBackgroundResource(R.color.SkyBlue);
	        tvHB.setText(Integer.toString(threadParams.records[i].hb));
	        
	        row.addView(tvTime);
	        row.addView(tvBPSys);
	        row.addView(tvBPDia);
	        row.addView(tvHB);
	        ll.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
	    }
	}
	
	
	public void startDateChanged(int startDatePos)
	{
		Spinner endDate = (Spinner) findViewById(R.id.spinnerEndDate);
		int endDatePos = endDate.getSelectedItemPosition();
		
		if (startDatePos > endDatePos)
		{
			endDatePos = startDatePos;
			endDate.setSelection(endDatePos);
		}
		
	    showRecords(startDatePos, endDatePos);
	}
	
	public void endDateChanged(int endDatePos)
	{
		Spinner startDate = (Spinner) findViewById(R.id.spinnerStartDate);
		int startDatePos = startDate.getSelectedItemPosition();	
				
		if (startDatePos > endDatePos)
		{
			startDatePos = endDatePos;
			startDate.setSelection(startDatePos);
		}
		
	    showRecords(startDatePos, endDatePos);	
	}
}
