package no.trommelyd.android;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Main program window, with proper listener registration.
 * 
 * @author torkildr
 */
public class MainProgram extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Volume control should adjust media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        // Main layout
        setContentView(R.layout.main);
        
        // Button to trigger event
        View button = findViewById(R.id.TrommelydButton);
        
        // This player will handle all relevant events
        TrommelydPlayer player = new TrommelydPlayer(getBaseContext());
        
        // Tell the button to bother someone else when clicked..
        if (button != null) {
            button.setOnClickListener(player);
        }
    }
    
}
