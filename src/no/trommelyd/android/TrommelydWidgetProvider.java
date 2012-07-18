package no.trommelyd.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

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
 * Widget provider, creates a pending intent to reroute the action to the service
 *
 * @author torkildr
 */
public class TrommelydWidgetProvider extends AppWidgetProvider {

    // Called when widget is updated / created
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // View to base widget on, buttons will be here
        RemoteViews mViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        // Intent to hold the service that will play the actual sound
        Intent mPlayerIntent = new Intent(context, TrommelydPlayerService.class);

        // Information to the service about what the intent is intended (ba-dom-tshh) to do
        mPlayerIntent.setAction(TrommelydPlayerService.ACTION_PLAY);

        // Make a pending intent targeted at our local service
        PendingIntent mPendingPlayerIntent = PendingIntent.getService(context, 0, mPlayerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Tell button that clicks will fire up then pending intent
        mViews.setOnClickPendingIntent(R.id.stick, mPendingPlayerIntent);
        
        appWidgetManager.updateAppWidget(appWidgetIds, mViews);
    }

}
