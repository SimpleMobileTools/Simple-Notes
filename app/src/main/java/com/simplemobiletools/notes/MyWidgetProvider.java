package com.simplemobiletools.notes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.simplemobiletools.notes.activities.MainActivity;

public class MyWidgetProvider extends AppWidgetProvider {
    private static SharedPreferences mPrefs;
    private static RemoteViews mRemoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initVariables(context);
        final int defaultColor = context.getResources().getColor(R.color.dark_grey);
        final int newBgColor = mPrefs.getInt(Constants.WIDGET_BG_COLOR, defaultColor);
        final int newTextColor = mPrefs.getInt(Constants.WIDGET_TEXT_COLOR, Color.WHITE);
        mRemoteViews.setInt(R.id.notes_view, "setBackgroundColor", newBgColor);
        mRemoteViews.setInt(R.id.notes_view, "setTextColor", newTextColor);

        for (int widgetId : appWidgetIds) {
            updateWidget(appWidgetManager, widgetId, mRemoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void initVariables(Context context) {
        mPrefs = context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        setupAppOpenIntent(R.id.notes_holder, context);
    }

    private void setupAppOpenIntent(int id, Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        mRemoteViews.setOnClickPendingIntent(id, pendingIntent);
    }

    private void updateWidget(AppWidgetManager widgetManager, int widgetId, RemoteViews remoteViews) {
        final String text = mPrefs.getString(Constants.TEXT, "");
        remoteViews.setTextViewText(R.id.notes_view, text);
        widgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
