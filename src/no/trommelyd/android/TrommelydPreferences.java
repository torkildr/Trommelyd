package no.trommelyd.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

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
 * Activity for saving preferences
 * 
 * @author torkildr
 */
public class TrommelydPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
    
}
