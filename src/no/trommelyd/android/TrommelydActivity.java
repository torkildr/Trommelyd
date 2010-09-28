package no.trommelyd.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
    
    // Dialog boxes
    public static final int DIALOG_ABOUT = 1;
    public static final int DIALOG_FIRST_RUN = 2;

    // Get service from binding
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBoundService = ((TrommelydPlayerService.TrommelydBinder) service).getService();
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
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        // Volume control should adjust media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        // Load main layout
        setContentView(R.layout.main);

        // Button to trigger event
        View mButton = findViewById(R.id.TrommelydButton);

        // Tell the button to bother someone else when clicked.. (also, how to bother them)
        if (mButton != null) {
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBoundService == null) {
                        Toast.makeText(getApplicationContext(),
                                "Ba-dom-tschhh (sorry, no sound)", Toast.LENGTH_SHORT).show();
                    } else {
                        mBoundService.playSound();
                    }
                }
            });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String text = sharedPref.getString("edittext_preference", "");

        Toast.makeText(this, "Text is: " + text, Toast.LENGTH_SHORT).show();
    }

    // Bind to service
    @Override
    protected void onStart() {
        super.onStart();
        
        // Intent to hold the service that will play the actual sound
        Intent mPlayerIntent = new Intent(this, TrommelydPlayerService.class);

        // We try to bind the player service
        bindService(mPlayerIntent, this, BIND_AUTO_CREATE);
    }
    
    // Clean up
    @Override
    protected void onStop() {
        super.onStop();

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
                    R.string.first_run, R.raw.first_run, true);
            break;
            
        default:
           dialog = null;
        
        }
        
        return dialog;
    }
    
}
