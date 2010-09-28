package no.trommelyd.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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

    // Creates a AlertDialog filled with content from file
    public static AlertDialog getAlertDialogFromFile(Context context,
            int titleResource, int fileResource, boolean linkify) {
        // Use alert dialog, because we can do a bunch of stuff with it
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
        BufferedReader br = new BufferedReader(new InputStreamReader(is), 1024);
        
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

}
