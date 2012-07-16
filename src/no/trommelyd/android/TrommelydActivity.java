package no.trommelyd.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

/*
 *    This file is part of Trommelyd for Android.
 *    Copyright (C) Torkild Retvedt
 *    http://app.trommelyd.no/
 *
 *    Trommelyd for Android is free software: you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as published
 *    by the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Trommelyd for Android is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License along
 *    with Trommelyd for Android. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Main program window, with proper listener registration.
 *
 * @author
 */
public class TrommelydActivity extends Activity implements ServiceConnection {

    // Binding to local service
    private TrommelydPlayerService mBoundService;

    // Sensor listener
    private TrommelydSensorListener mSensorListener;

    // Dialog boxes
    public static final int DIALOG_ABOUT = 1;
    public static final int DIALOG_FIRST_RUN = 2;
    
    // Indicates if externally called intent is already handled. Makes sure
    // that sound is only played once when called from browser.
    private boolean mLaunchPerformed = false;

    // Get service from binding
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBoundService = ((TrommelydPlayerService.TrommelydBinder) service).getService();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        
        // If launched directly via the "View" action, make some noise!
        if (getIntent().getAction().equals(Intent.ACTION_VIEW) && !mLaunchPerformed) {
        	playSound();
        	mLaunchPerformed = true;
        	return;
        }
        
        // User wants sound on startup
        if (sharedPref.getBoolean(TrommelydPreferences.PREF_STARTUP, false)) {
            playSound();
        }
    }

    // Remove reference to service
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBoundService = null;
    }

    // Called when the activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default preferences (only when first encountered)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Volume control should adjust media volume
        setVolumeControlStream(TrommelydPreferences.STREAM);

        // Load main layout
        setContentView(R.layout.main);

        // Button to trigger event
        View mButton = findViewById(R.id.TrommelydButton);

        // Tell the button to bother someone else when clicked.. (also, how to bother them)
        if (mButton != null) {
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	playSound();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Intent to hold the service that will play the actual sound
        Intent mPlayerIntent = new Intent(this, TrommelydPlayerService.class);

        // We try to bind the player service
        bindService(mPlayerIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // If user has opted in, show status bar
        toggleStatusBarVisability(sharedPref.getBoolean(TrommelydPreferences.PREF_STATUSBAR, true));

        // Set orientation
        setScreenOrientation(
                sharedPref.getString(TrommelydPreferences.PREF_ORIENTATION, "default"));

        boolean shake = sharedPref.getBoolean(TrommelydPreferences.PREF_SHAKE, false);

        if (shake) {
            // Sensor is not started, do this now
            if (mSensorListener == null) {
                enableShakeSensor();
            }

            if (mSensorListener != null && mSensorListener.hasSensor()) {
                Toast.makeText(this, R.string.preference_shake_text, Toast.LENGTH_SHORT).show();
            }
        } else if (mSensorListener != null) {
            // Sensor has been enabled, but is not to be used, disable it
            disableShakeSensor();
        }

        // Show this on "first" run
        if (sharedPref.getBoolean(TrommelydPreferences.PREF_FIRST, true)) {
            showDialog(DIALOG_FIRST_RUN);
            sharedPref.edit().putBoolean(TrommelydPreferences.PREF_FIRST, false).commit();
        }
    }

    // Clean up
    @Override
    protected void onStop() {
        super.onStop();

        // Disable shake sensor before disconnect service
        disableShakeSensor();

        // Removes binding to local service
        unbindService(this);
    }

    // Fill options menu (when pressing the Menu button)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // Menu item in options menu selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
            showDialog(DIALOG_ABOUT);
            return true;

        case R.id.preferences:
            Intent intent = new Intent(getBaseContext(), TrommelydPreferences.class);
            startActivity(intent);
            return true;

        case R.id.quit:
            // This won't actually do much, but onStop() will be called
            return moveTaskToBack(true);

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void setScreenOrientation(String orientation) {
        if (orientation.equals("default")) {
            // fixed default
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        } else if (orientation.equals("landscape")) {
            // fixed landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (orientation.equals("portrait")) {
            // fixed portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (orientation.equals("automatic")) {
            // automatic by sensor
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private void toggleStatusBarVisability(boolean statusbar) {
            int flag;

            if (!statusbar) {
                flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            } else {
                flag = 0;
            }

            // sets the application to full screen, thus removing the status bar
            // this can be done after layout is loaded
            getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       }

    private void enableShakeSensor() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Create sensor listener, does not start listening
        mSensorListener = new TrommelydSensorListener(this);

        // Set sensitivity from preference
        mSensorListener.setSensitivity(
                sharedPref.getInt(TrommelydPreferences.PREF_SENSITIVITY, 50));

        // Register callback for sensor, and start it when sensor is triggered
        mSensorListener.registerSensorChangeCallback(new Runnable() {
            @Override
            public void run() {
                playSound();
            }
        });

        mSensorListener.startListener();
    }

    private void disableShakeSensor() {
        // Reset orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        // Stop sensor listening
        if (mSensorListener != null) {
            mSensorListener.stopListener();
            mSensorListener.unregisterSensorChangeCallback();
            mSensorListener = null;
        }
    }

    // Play sounds "safely", that is without dangling services
    private void playSound() {
        if (mBoundService == null) {
            Toast.makeText(getApplicationContext(), R.string.sound_error,
                    Toast.LENGTH_SHORT).show();
        } else {
        	animateButton();
            mBoundService.playSound();
        }
    }

	private void animateButton() {
        View topStick = findViewById(R.id.StickTop);
        View bottomStick = findViewById(R.id.StickBottom);

        if (topStick != null && bottomStick != null) {
        	Animation animation = AnimationUtils.loadAnimation(this, R.anim.stick);

        	topStick.startAnimation(animation);
        	bottomStick.startAnimation(animation);
        }
	}

    // Create the different kinds of dialogs
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;

        switch(id) {

        case DIALOG_ABOUT:
            dialog = TrommelydHelper.getAlertDialogFromFile(this,
                    R.string.about, R.raw.about, true);
            break;

        case DIALOG_FIRST_RUN:
            dialog = TrommelydHelper.getAlertDialogFromFile(this,
                    R.string.first_run, R.raw.first_run, false);
            break;

        default:
           dialog = null;

        }

        return dialog;
    }

}
