package no.trommelyd.android;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * WIll play a sound on click even. Will restart sound when triggered before
 * sound is completed, and will prepare media for play ever completion.
 * 
 * @author torkildr
 */
class TrommelydPlayer implements OnClickListener, OnCompletionListener, OnPreparedListener {

	private MediaPlayer player;
	
	private Context context;

	// For some reason, we want to create the media player
	private synchronized void createMediaPlayer() {
        player = MediaPlayer.create(context, R.raw.trommelyd);
	}

	// Start playing sound
	@Override
	public synchronized void onClick(View v) {
		// If we don't know what sound to play, simply don't play it...
		if (player == null) {
			return;
		}
		
		// Either restart to start playing media (we restart so that we don't
		// get a bunch of queued plays that will go on forever)
    	if (player.isPlaying()) {
    		player.seekTo(0);
    	} else {
	    	player.start();
    	}
	}

	// Prepare next play when completed
	@Override
	public synchronized void onCompletion(MediaPlayer player) {
    	player.stop();
    	
    	// This will block (and since we're in a synch'ed method, this is 'good')
    	try {
    		player.prepare();
    	} catch (IOException e) {
    		// meh
    		return;
    	}
	}

	// Finished preparing, seek to start of file
	@Override
	public void onPrepared(MediaPlayer player) {
		player.seekTo(0);
	}
	
	// Load media for first play
	public TrommelydPlayer(Context context) {
		// For safe-keeping
		this.context = context;
		
		// Load media file
		createMediaPlayer();

        // When playback is complete, prepare for next play
    	player.setOnCompletionListener(this);
    	
    	// When media is prepared, make sure position is correct
    	player.setOnPreparedListener(this);
	}
	
}
