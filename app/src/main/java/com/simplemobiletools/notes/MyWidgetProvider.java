package com.simplemobiletools.notes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
    private static SharedPreferences prefs;
    private RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initVariables(context);
        final int defaultColor = context.getResources().getColor(R.color.dark_grey);
        final int newBgColor = prefs.getInt(Constants.WIDGET_BG_COLOR, defaultColor);
        final int newTextColor = prefs.getInt(Constants.WIDGET_TEXT_COLOR, Color.WHITE);
        remoteViews.setInt(R.id.notes_view, "setBackgroundColor", newBgColor);
        remoteViews.setInt(R.id.notes_view, "setTextColor", newTextColor);

        for (int widgetId : appWidgetIds) {
            updateWidget(appWidgetManager, widgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void initVariables(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        setupIntent(R.id.notes_holder, context);
    }

    private void setupIntent(int id, Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(id, pendingIntent);
    }

    private void updateWidget(AppWidgetManager widgetManager, int widgetId, RemoteViews remoteViews) {
        final String text = prefs.getString(Constants.TEXT, "");
        remoteViews.setTextViewText(R.id.notes_view, text);
        widgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
