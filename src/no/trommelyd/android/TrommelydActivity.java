package no.trommelyd.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
                        return;
                    }
                    
                    mBoundService.playSound();
                }
            });
        }
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
        inflater.inflate(R.menu.trommelyd_menu, menu);
        return true;
    }
    
    // Menu item in options menu selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
            showDialog(DIALOG_ABOUT);
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
            // Use alert dialog, because we can do a bunch of stuff with it
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Inflate layout
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_dialog, null);
            
            // Set title and view
            builder.setTitle(R.string.menu_about);
            builder.setView(layout);

            // Grab text and linkify it
            SpannableString text = new SpannableString(readFile(this, R.raw.about));
            Linkify.addLinks(text, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);

            // Place text and make it clickable
            TextView textView = (TextView) layout.findViewById(R.id.custom_dialog_text);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(text);

            // Button
            builder.setNegativeButton(R.string.menu_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            dialog = builder.create();
            break;
        default:
           dialog = null;
        }
        
        return dialog;
    }
    
    // Helper method for reading file contents from resource
    private String readFile(Context context, int resource) {
        InputStream is = context.getResources().openRawResource(resource);
        BufferedReader br = new BufferedReader(new InputStreamReader(is), 1024);
        
        StringBuffer content = new StringBuffer();

        try {
            char[] buffer = new char[256];

            int size;

            // Need this instead of readLine (we need those \n's!!)
            while ((size = br.read(buffer)) != -1) {
                content.append(buffer, 0, size);
            }
            
        } catch (IOException e) {
            return "Oops..";
        }

        return content.toString();
    }

}
