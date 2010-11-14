/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license
 * 
 * Modified by Torkild Retvedt
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.hlidskialf.android.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements
        SeekBar.OnSeekBarChangeListener {
    
    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private SeekBar mSeekBar;
    private TextView mSplashText, mValueText;
    private Context mContext;

    private String mDialogMessage, mSuffix;
    
    private int mDefault;
    private int mMin;
    private int mMax;
    private int mValue = 0;
    private int mTextSize;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
        mSuffix = attrs.getAttributeValue(androidns, "text");
        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
        mMin = attrs.getAttributeIntValue(null, "min", 0);
        mMax = attrs.getAttributeIntValue(androidns, "max", 100);
        mTextSize = attrs.getAttributeIntValue(androidns, "textSize", 20);
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        mSplashText = new TextView(mContext);

        // Only add if we supply a text
        if (mDialogMessage != null) {
            mSplashText.setText(mDialogMessage);
            layout.addView(mSplashText);
        }

        // If we don't supply this, suppress the whole damn thing
        if (mSuffix != null) {
            mValueText = new TextView(mContext);
            mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
            mValueText.setTextSize(mTextSize);
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.addView(mValueText, params);
        }

        // Seek bar stuff
        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (shouldPersist())
            mValue = getPersistedInt(mDefault);

        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(mValue + mMin);
        
        return layout;
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // Only if OK is pressed
        if (positiveResult)
            persistInt(mSeekBar.getProgress() + mMin);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(mValue - mMin);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore)
            mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
        else
            mValue = (Integer) defaultValue;
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        if (mSuffix != null) {
            String t = String.valueOf(value + mMin);
            mValueText.setText(t.concat(mSuffix));
        }
    }

    public void onStartTrackingTouch(SeekBar seek) {
    }

    public void onStopTrackingTouch(SeekBar seek) {
    }

}
