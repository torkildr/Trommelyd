package no.trommelyd.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main program window, with proper listener registration.
 * 
 * @author torkildr
 */
public class TrommelydActivity extends Activity implements ServiceConnection {
    
    // Binding to local service
    private TrommelydPlayerService mBoundService;
    
    // Dialog boxes
    public static final int DIALOG_ABOUT = 1;

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

        // Remove title bar (TODO: move to xml?)
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Volume control should adjust media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        // Load main layout
        setContentView(R.layout.main);
        
        // Button to trigger event
        View mButton = findViewById(R.id.TrommelydButton);

        // Intent to hold the service that will play the actual sound
        Intent mPlayerIntent = new Intent(this, TrommelydPlayerService.class);

        // We try to bind the player service
        if (!bindService(mPlayerIntent, this, BIND_AUTO_CREATE)) {
            Toast.makeText(getApplicationContext(),
                    "Ba-dom-tschhh (sorry, no sound)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tell the button to bother someone else when clicked.. (also, how to bother them)
        if (mButton != null) {
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBoundService.playSound();
                }
            });
        }
    }
    
    // Clean up
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Removes binding to local service
        unbindService(this);
    }
    
    // Fill options menu (when pressing the Menu button)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trommelyd_menu, menu);
        return true;
    }
    
    // Menu item in options menu selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
            return showDialog(DIALOG_ABOUT, null);
            
        case R.id.quit:
            // TODO: We would probably want to unbind the service here as well
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
            // Make dialog, set view and title
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.custom_dialog);
            dialog.setTitle(R.string.menu_about);
            
            // Fill text
            TextView text = (TextView) dialog.findViewById(R.id.custom_dialog_text);
            text.setText(readFile(this, R.raw.about));
            
            break;
        default:
           dialog = null;
        }
        
        return dialog;
    }
    
    // Helper method for reading file contents from resource
    // TODO: Make this shiny
    private String readFile(Context context, int resource) {
        InputStream is = context.getResources().openRawResource(resource);
        BufferedReader br = new BufferedReader(new InputStreamReader(is), 1024);
        
        StringBuffer content = new StringBuffer();

        try {
            String readLine = null;

            while ((readLine = br.readLine()) != null) {
                content.append(readLine);
            }
        } catch (IOException e) {
            return "Oops..";
        }

        return content.toString();
    }

}
