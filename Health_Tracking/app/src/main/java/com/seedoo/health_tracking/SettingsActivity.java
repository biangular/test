package com.seedoo.health_tracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);
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
		case R.id.action_close:
			startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
