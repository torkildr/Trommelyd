package no.trommelyd.android;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class trommelyd extends Activity {

	private volatile MediaPlayer trommelydPlayer;
	
	// Event handler for button click
	private OnClickListener trommelydButtonListener = new OnClickListener() {
	    public void onClick(View v) {
	    	if (trommelydPlayer.isPlaying()) {
	    		trommelydPlayer.seekTo(0);
	    	} else {
		    	trommelydPlayer.start();
	    	}
	    }
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove notification bar
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// main layout
        setContentView(R.layout.main);
        
        // Capture our button from layout
        ImageView trommelydButton = (ImageView)findViewById(R.id.TrommelydButton);
	    
	    // Register the onClick listener with the implementation above
	    if (trommelydButton != null)
	    	trommelydButton.setOnClickListener(trommelydButtonListener);
	    
        trommelydPlayer = MediaPlayer.create(getBaseContext(), R.raw.trommelyd);

        // when playback is complete, prepare for next play
    	trommelydPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            	mp.stop();
            	mp.prepareAsync();
            }
        });
    	
    	// when media is prepared, make sure position is correct
    	trommelydPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.seekTo(0);
			}
		});
    }
    
}