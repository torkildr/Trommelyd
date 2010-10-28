package no.trommelyd.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
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
 * Activity for saving preferences, also some info...
 * 
 * @author torkildr
 */
public class TrommelydPreferences extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    public static final String PREF_MUTED = "muted";
    public static final String PREF_COUNT = "count";
    public static final String PREF_REPEAT = "repeat";
    public static final String PREF_STARTUP = "startup";
    public static final String PREF_FIRST = "first_run";
    
    // Keep track of changes
    private boolean mIsChanged = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Grab preferences and register the onChange listener
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        // Preferences
        addPreferencesFromResource(R.xml.preferences);

        // Add dynamic info for this one
        Preference intentPref = getPreferenceScreen().findPreference("version_info");

        if (intentPref != null) {
            // Intent for preference, open web page
            intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
                    .setData(Uri.parse(getText(R.string.url).toString())));
            
            // Version name
            String version = TrommelydHelper.getVersionNumber(this);
            intentPref.setTitle(getString(R.string.app_name) + 
                    ((version.length() > 0) ? " v" + version : ""));
    
            // Number of plays
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            int count = sharedPref.getInt(PREF_COUNT, 0);
            intentPref.setSummary("Sound played " + count + " time" +
                    ((count > 1) ? "s" : ((count == 0) ? "s" : "")));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mIsChanged = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mIsChanged) {
            Toast.makeText(this, R.string.preference_saved, Toast.LENGTH_SHORT).show();
        }
    }
    
}
