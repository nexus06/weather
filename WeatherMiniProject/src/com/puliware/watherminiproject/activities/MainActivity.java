package com.puliware.watherminiproject.activities;


import com.puliware.watherminiproject.R;

import com.puliware.watherminiproject.R.id;
import com.puliware.watherminiproject.R.layout;
import com.puliware.watherminiproject.R.menu;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.puliware.watherminiproject.operations.WeatherOps;
import com.puliware.watherminiproject.operations.WeatherOpsImpl;
import com.puliware.watherminiproject.utils.RetainedFragmentManager;;


/**
 * The main Activity that prompts the user for Weather to expand via
 * various implementations of AcronymServiceSync and
 * AcronymServiceAsync and view via the results.  Extends
 * LifecycleLoggingActivity so its lifecycle hook methods are logged
 * automatically.
 */
public class MainActivity extends LifecycleLoggingActivity{
	
	/**
     * Used to retain the state between runtime configuration
     * changes. Handles rotations
     */
    protected final RetainedFragmentManager mRetainedFragmentManager = 
        new RetainedFragmentManager(this.getFragmentManager(),
                                    TAG);
    
    /**
     * Provides weather-related operations.
     */
    private WeatherOps mWeatherOps;

    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 // Create the AcronymOps object one time.
        mWeatherOps = new WeatherOpsImpl(this);

        // Handle any configuration change.
        handleConfigurationChanges();
	}
	

    /**
     * Hook method called by Android when this Activity is
     * destroyed.
     */
    @Override
    protected void onDestroy() {
        // Unbind from the Service.
    	mWeatherOps.unbindService();

        // Always call super class for necessary operations when an
        // Activity is destroyed.
        super.onDestroy();
    }
    
    /*
     * Initiate the synchronous acronym lookup when the user presses
     * the "Look Up Sync" button.
     */
    public void getCurrentWeatherSync(View v) {
    	mWeatherOps.getCurrentWeatherSync(v);
    }

    /*
     * Initiate the asynchronous acronym lookup when the user presses
     * the "Look Up Async" button.
     */
    public void getCurrentWeatherAsync(View v) {
    	mWeatherOps.getCurrentWeatherAsync(v);
    }
    
	
	 /**
     * Handle hardware reconfigurations, such as rotating the display.
     */
    protected void handleConfigurationChanges() {
        // If this method returns true then this is the first time the
        // Activity has been created.
        if (mRetainedFragmentManager.firstTimeIn()) {
            Log.d(TAG,
                  "First time onCreate() call");

            // Create the AcronymOps object one time.  The "true"
            // parameter instructs AcronymOps to use the
            // DownloadImagesBoundService.
            mWeatherOps = new WeatherOpsImpl(this);

            // Store the AcronymOps into the RetainedFragmentManager.
            mRetainedFragmentManager.put("ACRONYM_OPS_STATE",
            		mWeatherOps);
            
            // Initiate the service binding protocol (which may be a
            // no-op, depending on which type of DownloadImages*Service is
            // used).
            mWeatherOps.bindService();
        } else {
            // The RetainedFragmentManager was previously initialized,
            // which means that a runtime configuration change
            // occured.

            Log.d(TAG,
                  "Second or subsequent onCreate() call");

            // Obtain the AcronymOps object from the
            // RetainedFragmentManager.
            mWeatherOps = 
                mRetainedFragmentManager.get("ACRONYM_OPS_STATE");

            // This check shouldn't be necessary under normal
            // circumtances, but it's better to lose state than to
            // crash!
            if (mWeatherOps == null) {
                // Create the AcronymOps object one time.  The "true"
                // parameter instructs AcronymOps to use the
                // DownloadImagesBoundService.
            	mWeatherOps = new WeatherOpsImpl(this);

                // Store the AcronymOps into the RetainedFragmentManager.
                mRetainedFragmentManager.put("ACRONYM_OPS_STATE",
                		mWeatherOps);

                // Initiate the service binding protocol (which may be
                // a no-op, depending on which type of
                // DownloadImages*Service is used).
                mWeatherOps.bindService();
            } else
                // Inform it that the runtime configuration change has
                // completed.
            	mWeatherOps.onConfigurationChange(this);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
