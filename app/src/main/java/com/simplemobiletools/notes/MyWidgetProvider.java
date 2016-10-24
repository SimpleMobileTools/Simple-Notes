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
import com.simplemobiletools.notes.databases.DBHelper;
import com.simplemobiletools.notes.models.Note;

import static com.simplemobiletools.notes.R.layout.widget;

public class MyWidgetProvider extends AppWidgetProvider {
    private DBHelper mDb;
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
        mRemoteViews.setFloat(R.id.notes_view, "setTextSize", Utils.getTextSize(context) / context.getResources().getDisplayMetrics().density);

        for (int widgetId : appWidgetIds) {
            updateWidget(appWidgetManager, widgetId, mRemoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void initVariables(Context context) {
        mPrefs = context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        mDb = DBHelper.newInstance(context);
        mRemoteViews = new RemoteViews(context.getPackageName(), widget);
        setupAppOpenIntent(R.id.notes_holder, context);
    }

    private void setupAppOpenIntent(int id, Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        mRemoteViews.setOnClickPendingIntent(id, pendingIntent);
    }

    private void updateWidget(AppWidgetManager widgetManager, int widgetId, RemoteViews remoteViews) {
        final int widgetNoteId = mPrefs.getInt(Constants.WIDGET_NOTE_ID, 1);
        final Note note = mDb.getNote(widgetNoteId);
        remoteViews.setTextViewText(R.id.notes_view, note != null ? note.getValue() : "");
        widgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
