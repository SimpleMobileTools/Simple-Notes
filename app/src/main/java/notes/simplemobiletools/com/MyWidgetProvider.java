package notes.simplemobiletools.com;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
    private static SharedPreferences prefs;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private void updateWidget(Context context, AppWidgetManager widgetManager, int widgetId) {
        final String text = prefs.getString(Constants.TEXT, "");
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setTextViewText(R.id.notesView, text);
        widgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
