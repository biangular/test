package com.seedoo.health_tracking;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.graphics.Color;
//import android.widget.TextView;

public class SignupActivity extends ActionBarActivity implements OnClickListener {
	
	Button btnCreateAccount;
	
    private EditText  editTextUserName=null;
    private EditText  editTextEmail=null;
    private EditText  editTextPassword=null;
    private EditText  editTextConfirmPassword=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
       
        // Get References of Views
        editTextUserName=(EditText)findViewById(R.id.etUserName);
        editTextEmail=(EditText)findViewById(R.id.etEmail);
        editTextPassword=(EditText)findViewById(R.id.etPass);
        editTextConfirmPassword=(EditText)findViewById(R.id.etPassAgain);
 
        btnCreateAccount=(Button)findViewById(R.id.buttonCreateAccount);
        
        btnCreateAccount.setOnClickListener(this);
    }
	
	
	@Override
	public void onClick(View v) {
		
		
		SignupThreadParams params = new SignupThreadParams();
		
		params.username = this.editTextUserName.getText().toString();
        params.email = this.editTextEmail.getText().toString();
        params.password = this.editTextPassword.getText().toString();
        String confirmPassword = this.editTextConfirmPassword.getText().toString();

        // check if any of the fields are vacant
        if("".equals(params.username) || 
           "".equals(params.email) ||
           "".equals(params.password) || 
           "".equals(confirmPassword))
        {
            Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_LONG).show();
            return;
        }
        
        // check if both password matches
        if(!params.password.equals(confirmPassword))
        {
            Toast.makeText(getApplicationContext(), "Matching password required", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (!isEmailValid(params.email))
        {
        	Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_LONG).show();
            return;
        }
        
        SignupThread thread = new SignupThread(params);
        try {
        	thread.start();
        	thread.join();
        }
        catch (Exception e)
        {
        	Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        	return;
        }
        
        if ("OK".equals(params.rc) == false)
        {
        	Toast.makeText(getApplicationContext(), params.reason, Toast.LENGTH_LONG).show();
        	return;
        }
        // Successful
        Toast.makeText(getApplicationContext(), "Account Created", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_signup, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_login:
			startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
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
	
	public boolean isEmailValid(String email) {
	    boolean isValid = false;

	    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	    CharSequence inputStr = email;

	    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(inputStr);
	    if (matcher.matches()) {
	        isValid = true;
	    }
	    return isValid;
	}
}
