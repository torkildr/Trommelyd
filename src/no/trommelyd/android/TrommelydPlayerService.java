package no.trommelyd.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
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
 * This service can be bound or triggered by pending intent. Holds and uses the
 * media player to play the sound.
 * 
 * @author torkildr
 */
public class TrommelydPlayerService extends Service
        implements OnSharedPreferenceChangeListener {

    // To minimize latency, we keep track of preferences
    private SharedPreferences mSharedPrefs;
    
    // Service actions
    public static final String ACTION_PLAY = "play";
    
    // Time, in ms, to wait if sound is not loaded
    public static final int WAIT_TIME = 1500;
    
    // Pre-fetched preference variables
    private boolean mPlayMuted;
    private int mCount;
    private boolean mRepeat;
    private int mDelay;
    
    // Stream / pool
    private SoundPool mPool;

    private int[] mSounds = { -1 };
    private int[] mStreams = { -1 };
    private final int[] mResources = { R.raw.trommelyd };

    // We use this to keep track of the sound, as SoundPool does not support this
    private long[] mStartTime = { -1 };

    // List of streams we have
    public static final int STREAM_TROMMELYD = 0;
    
    // Binder for local service calls
    public final IBinder mBinder = new TrommelydBinder();

    public class TrommelydBinder extends Binder {
        public TrommelydPlayerService getService() {
            return TrommelydPlayerService.this;
        }
    }
    
    // For some reason, we want to create the sound pool
    private synchronized boolean createMediaPlayer() {
        if (mPool != null) {
            mPool.release();
            mPool = null;
        }

        // Create the sound pool; number of streams, play as music, default quality
        mPool = new SoundPool(mStreams.length, AudioManager.STREAM_MUSIC, 0);
        
        // Load the stream(s)
        if (mPool != null) {
            // 1 == default priority, "future compatibility"
            mSounds[STREAM_TROMMELYD] = mPool.load(this, mResources[STREAM_TROMMELYD], 1); 

            return true;
        } else {
            return false;
        }
    }
    
    // Play sound, either called via local service interface or directly
    public synchronized void playSound(int stream) {
        // If we don't know what sound to play, simply don't play it...
        if (mPool == null) {
            Toast.makeText(getApplicationContext(), R.string.sound_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Don't play sound if we've asked for it and it's not in normal mode
        if (!mPlayMuted) {
            // Get audio manager, need this for reading sound state
            AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

            if (manager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                return;
            }
        }
        
        long startWait = System.currentTimeMillis();
        
        // In the off-chance that the sound is not yet loaded, we wait for it to do so
        while (mSounds[stream] == -1 && (System.currentTimeMillis() - startWait) < WAIT_TIME);
        
        // Sound is playing
        if (mStartTime[stream] > 0) {
            // Check if sound has been playing for long
            if ((System.currentTimeMillis() - mStartTime[stream]) > mDelay) {
                // User has opted out
                if (!mRepeat) {
                    return;
                } else {
                    // Stop previous playing stream
                    mPool.stop(mStreams[stream]);
                    mStreams[stream] = startStream(stream);
                }
            } else {
                // Too soon!
                return;
            }
        } else {
            // Start playing sound
            mStreams[stream] = startStream(stream);
        }

        // Since we're still here, we've obviously played the sound, count it
        mSharedPrefs.edit().putInt(TrommelydPreferences.PREF_COUNT, mCount+1).commit();
    }

    private int startStream(int stream) {
        // Store start time
        mStartTime[stream] = System.currentTimeMillis();

        // Play sound, sound-id, l/r volume, priority, loop, rate
        return mPool.play(mSounds[stream], 1.0f, 1.0f, 0, 0, 1.0f);
    }
   
    // Service is created, prepare media player
    @Override
    public void onCreate() {
        // Load media file
        if (!createMediaPlayer()) {
            return;
        }
        
        // Grab preferences and register the onChange listener
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
        
        // Load preferences into pre-fetched variables
        onSharedPreferenceChanged(mSharedPrefs, TrommelydPreferences.PREF_COUNT);
        onSharedPreferenceChanged(mSharedPrefs, TrommelydPreferences.PREF_MUTED);
        onSharedPreferenceChanged(mSharedPrefs, TrommelydPreferences.PREF_REPEAT);
        onSharedPreferenceChanged(mSharedPrefs, TrommelydPreferences.PREF_DELAY);
    }
    
    // Service is destroyed, attempt to clean up...
    @Override
    public void onDestroy() {
        // Release media player resources
        if (mPool != null) {
            mPool.release();
            mPool = null;
        }

        // Clean up storage for preferences
        if (mSharedPrefs != null) {
            mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
            mSharedPrefs = null;
        }
    }

    // Handle commands, for now only play
    private void handleCommand(Intent intent) {
        // Someone stuffed this in the intent, obey!
        if (intent.getAction().equals(ACTION_PLAY)) {
            playSound(STREAM_TROMMELYD);
        }
    }
    
    // Backwards compatibility for API < 5
    @Override
    public void onStart(Intent intent, int startId) {
        Log.d("Trommelyd", "Command (API < 5) from intent: " + intent.toURI());
        handleCommand(intent);
    }
    
    // Used for API >= 5
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Trommelyd", "Command from intent: " + intent.toURI());
        handleCommand(intent);
        
        // Throw away when killed
        return Service.START_NOT_STICKY;
    }
    
    // Give a simple binder for local calls
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Trommelyd", "Service bound from intent: " + intent.toURI());
        return mBinder;
    }

    // Pre-fetch changes from preferences
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(TrommelydPreferences.PREF_MUTED)) {
            mPlayMuted = prefs.getBoolean(TrommelydPreferences.PREF_MUTED, true);
        } else if (key.equals(TrommelydPreferences.PREF_COUNT)) {
            mCount = prefs.getInt(TrommelydPreferences.PREF_COUNT, 0);
        } else if (key.equals(TrommelydPreferences.PREF_REPEAT)) {
            mRepeat = prefs.getBoolean(TrommelydPreferences.PREF_REPEAT, true);
        } else if (key.equals(TrommelydPreferences.PREF_DELAY)) {
            mDelay = prefs.getInt(TrommelydPreferences.PREF_DELAY, 500);
        }
    }
        
}

