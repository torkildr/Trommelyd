package no.trommelyd.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TrommelydSensorListener implements SensorEventListener {

    private SensorManager mSensorManager;
    private Runnable mCallback;
    
    // TODO: change this to preference, already exists, only lacks the logic.
    // also, range?? min: 2-3, max: 9-10?
    private static final double MIN_THRESHOLD = 1.5f;
    private static final double MAX_THRESHOLD = 9.5f;
    
    // Sensitivity, typically set by user (set to infinite so things do go crazy by default)
    private double mSensitivity = Double.POSITIVE_INFINITY;
    
    // Delta gravity
    private float mAcceleration = 0.0f;
    // Current gravity
    private float mCurrentGravity = SensorManager.GRAVITY_EARTH;
    // Last known gravity
    private float mLastGravity = SensorManager.GRAVITY_EARTH;
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Currently we don't need to do anything here..
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Gravitation in all directions
        float x = event.values[SensorManager.DATA_X];
        float y = event.values[SensorManager.DATA_Y];
        float z = event.values[SensorManager.DATA_Z];
        
        // Store current
        mLastGravity = mCurrentGravity;
        
        // Calculate current gravity
        mCurrentGravity = (float) Math.sqrt(x*x + y*y + z*z);
        
        // Changed since last known gravity
        float delta = mCurrentGravity - mLastGravity;
        
        // Smooth the changes
        mAcceleration = (mAcceleration * 0.9f) + delta;
        
        // Above threshold, trigger call-back
        if (mAcceleration > mSensitivity && mCallback != null) {
            Log.d("Trommelyd", "Sensor changed, current value: " + mAcceleration + ", delta: " + mAcceleration);
            Log.d("Trommelyd", "Sensor threshold: " + mSensitivity);
            
            mCallback.run();
            Log.d("Trommelyd", "Sensor threshold reached: " + mAcceleration);
        }
    }

    public TrommelydSensorListener(Context context) {
        // Grab the sensor service
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Log.d("Trommelyd", "Sensor started: " + mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER));
    }
    
    public void registerSensorChangeCallback(Runnable runnable) {
        mCallback = runnable;
    }
    
    public void unregisterSensorChangeCallback() {
        mCallback = null;
    }
    
    public void startListener() {
        // Register sensor listener, maybe this class, per chance?
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        Log.d("Trommelyd", "Sensor listener started");
    }

    public void stopListener() {
        // Unregister listener
        mSensorManager.unregisterListener(this);
        
        Log.d("Trommelyd", "Sensor listener stopped");
    }
    
    // Sets the sensitivity between MAX_THRESHOLD and MIN_THRESHOLD, given a percentage
    public void setSensitivity(int percentage) {
        if (percentage > 100 || percentage < 0)
            percentage = 50;

        double factor = ((double) percentage) / 100;
        double current = MIN_THRESHOLD + ((MAX_THRESHOLD - MIN_THRESHOLD) * factor);
        
        mSensitivity = (MIN_THRESHOLD + MAX_THRESHOLD) - current;
    }
    
}
