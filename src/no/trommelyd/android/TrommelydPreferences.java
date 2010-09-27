package no.trommelyd.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TrommelydPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
    
}
