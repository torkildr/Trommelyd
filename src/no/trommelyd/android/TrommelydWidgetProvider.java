package no.trommelyd.android;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TrommelydWidgetProvider extends AppWidgetProvider {

    // Called when widget is updated / created
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // View to base widget on, buttons will be here
        RemoteViews mViews = new RemoteViews(context.getPackageName(), R.layout.trommelyd_widget);

        // Intent to hold the service that will play the actual sound
        Intent mPlayerIntent = new Intent(context, TrommelydPlayerService.class);

        // Information to the service about what the intent is intended (ba-dom-tshh) to do
        mPlayerIntent.setAction(TrommelydPlayerService.ACTION_PLAY);

        // Make a pending intent targeted at our local service
        PendingIntent mPendingPlayerIntent = PendingIntent.getService(context, 0, mPlayerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Tell button that clicks will fire up then pending intent
        mViews.setOnClickPendingIntent(R.id.TrommelydWidgetButton, mPendingPlayerIntent);
        
        appWidgetManager.updateAppWidget(appWidgetIds, mViews);
    }
    
}
