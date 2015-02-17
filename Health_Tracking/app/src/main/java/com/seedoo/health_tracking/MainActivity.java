package com.seedoo.health_tracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	public static String logged_in_user;
	public static String logged_in_user_cat;
	Button btnSignIn;
	Button btnSignUp;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*
        if (getIntent().getBooleanExtra("EXIT", false)) {
        	finish();
        	android.os.Process.killProcess(android.os.Process.myPid());
        }
        */
        
        setContentView(R.layout.activity_main);
        
        logged_in_user = null;
        logged_in_user_cat = null;
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        
        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }
    
    
	@Override
	public void onClick(View v) {
		Intent i = null;
		switch(v.getId()){
			case R.id.btnSignIn:
				i = new Intent(this,LoginActivity.class);
				break;
			case R.id.btnSignUp:
				i = new Intent(this,SignupActivity.class);
				break;
		}
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_login:
			startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		    return true;
		case R.id.action_signup:
			startActivity(new Intent(this, SignupActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		    return true;
		case R.id.action_show_rec:
			if (MainActivity.logged_in_user == null)
			{
				startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
			else
			{
				startActivity(new Intent(this, ShowRecActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
		    return true;
		case R.id.action_exit:
			exitApp();
		    return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	public void exitApp()
	{
		LogoutThread logoutThread = new LogoutThread(null);
		try {
			logoutThread.start();
			logoutThread.join();
			MainActivity.logged_in_user = null;
			MainActivity.logged_in_user_cat = null;
			/*
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);  
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("EXIT", true);
			try {
				startActivity(intent);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			*/
			super.onBackPressed();
			//android.os.Process.killProcess(android.os.Process.myPid());	
    	} 
        catch (Exception e) 
    	{
    		e.printStackTrace();
    	}
	}	
}
