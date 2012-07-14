package no.trommelyd.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.media.AudioManager;

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
 * Helper functions
 *
 * @author torkildr
 */
public class TrommelydHelper {

	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final Charset LATIN1 = Charset.forName("iso8859-1");
	
    // Creates a AlertDialog filled with content from file
    public static AlertDialog getAlertDialogFromFile(Context context, int titleResource, int fileResource, boolean linkify) {
        // Use alert dialog, because we can do a bunch of stuff with it
    	AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.DrumsoundTheme));

        // Inflate layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_dialog, null);

        // Set title and view
        builder.setTitle(titleResource);
        builder.setView(layout);

        // Grab text and linkify it
        SpannableString text = new SpannableString(readFile(context, fileResource));

        if (linkify)
            Linkify.addLinks(text, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);

        // Place text and make it clickable
        TextView textView = (TextView) layout.findViewById(R.id.custom_dialog_text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);

        // Button
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    // Read file from resource id
    public static String readFile(Context context, int resource) {
        InputStream is = context.getResources().openRawResource(resource);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, TrommelydHelper.LATIN1), 1024);

        StringBuffer content = new StringBuffer();

        try {
            char[] buffer = new char[256];

            int size;

            // Need this instead of readLine (we need those \n's!!)
            while ((size = br.read(buffer)) != -1) {
                content.append(buffer, 0, size);
            }
        } catch (IOException e) {
            return "Oops..";
        }

        return content.toString();
    }

    // Get version number from manifest/package info
    public static String getVersionNumber(Context context) {
        try {
            String pkg = context.getPackageName();
            return context.getPackageManager().getPackageInfo(pkg, 0).versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }
    
    // Gets current volume, in percent
    public static float getVolume(Context context) {
    	AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    	int volume = audioManager.getStreamVolume(TrommelydPreferences.STREAM);
    	int max = audioManager.getStreamMaxVolume(TrommelydPreferences.STREAM);
    	
    	return (volume / (float) max) * 100;
    }

    // Sets volume, in percent
    public static void setVolume(Context context, float percent) {
    	AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    	int max = audioManager.getStreamMaxVolume(TrommelydPreferences.STREAM);
    	int volume = Math.round(max * (percent / 100));
    	
        Log.d("Trommelyd", "Setting volume to: " + volume);
		audioManager.setStreamVolume(TrommelydPreferences.STREAM, volume, 0);
    }
}
