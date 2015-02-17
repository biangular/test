package com.seedoo.health_tracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends ActionBarActivity implements OnClickListener {
	private TextView attemptsTxtVw;
	private int counter = 3;
	
	
	@Override
	public void onClick(View v) {
        // get the References of views
        final  EditText editTextUserName=(EditText)this.findViewById(R.id.editTextUserNameToLogin);
        final  EditText editTextPassword=(EditText)this.findViewById(R.id.editTextPasswordToLogin);

        LoginThreadParams threadParams = new LoginThreadParams();
        threadParams.username = editTextUserName.getText().toString();
        threadParams.password = editTextPassword.getText().toString();
        LoginThread loginThread = new LoginThread(threadParams);
        
        try
        
        {
        	loginThread.start();
        	loginThread.join();
        
			
    		if (threadParams.rc.equals("OK") == false)
    		{
    			MainActivity.logged_in_user = null;
    			MainActivity.logged_in_user_cat = null;
    			this.counter -= 1;
    			if (this.counter == 0)
    			{
    				// kill this app
    				android.os.Process.killProcess(android.os.Process.myPid());
    			}
    			else
    			{
    			    this.attemptsTxtVw = (TextView)findViewById(R.id.textView5);
    			    this.attemptsTxtVw.setText(Integer.toString(this.counter));
    			}
    		}
    		else
    		{
    			MainActivity.logged_in_user = threadParams.username;
    			MainActivity.logged_in_user_cat = threadParams.usercat;
    			Intent i = new Intent(this,ShowRecActivity.class);
    			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			startActivity(i);
    		}
                    
    	} 
        catch (Exception e) 
    	{
    		e.printStackTrace();
    	}           	
    }
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	    this.attemptsTxtVw = (TextView)findViewById(R.id.textView5);
	    this.attemptsTxtVw.setText(Integer.toString(this.counter));
        Button btnSignIn=(Button)this.findViewById(R.id.buttonSignIn);
        btnSignIn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_login, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
		case R.id.action_signup:
			startActivity(new Intent(this, SignupActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		    return true;
		    
		case R.id.action_show_rec:
			//if (MainActivity.logged_in_user == null)
			//{
				//startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			//}
			//else
			{
				startActivity(new Intent(this, ShowRecActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			}
			return true;
		case R.id.action_close:
			startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			finish();
		    return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
		
	
	@Override
    protected void onDestroy() {
	    super.onDestroy();
    }
}

