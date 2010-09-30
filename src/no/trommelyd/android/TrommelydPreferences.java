package no.trommelyd.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
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
public class TrommelydPreferences extends PreferenceActivity {

    public static final String PREF_MUTED = "muted";
    public static final String PREF_COUNT = "count";
    public static final String PREF_REPEAT = "repeat";
    public static final String PREF_STARTUP = "startup";
    public static final String PREF_FIRST = "first_run";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // New category
        PreferenceCategory infoCategory = new PreferenceCategory(this);
        infoCategory.setTitle(R.string.info);

        getPreferenceScreen().addPreference(infoCategory);
        
        // Insert extra preference with dynamic information
        PreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
        infoCategory.addPreference(intentPref);
        
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
    
    @Override
    protected void onStop() {
        super.onStop();
        
        Toast.makeText(this, R.string.preference_saved, Toast.LENGTH_SHORT).show();
    }
    
}
