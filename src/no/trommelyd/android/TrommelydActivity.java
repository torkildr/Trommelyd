package no.trommelyd.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Main program window, with proper listener registration.
 * 
 * @author torkildr
 */
public class TrommelydActivity extends Activity implements ServiceConnection {
    
    // Binding to local service
    private TrommelydPlayerService mBoundService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBoundService = ((TrommelydPlayerService.TrommelydBinder) service).getService();
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBoundService = null;
    }

    /** Called when the activity is first created. */
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
    
}
