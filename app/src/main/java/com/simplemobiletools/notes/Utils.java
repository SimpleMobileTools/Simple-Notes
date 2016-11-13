package com.simplemobiletools.notes;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.Toast;

public class Utils {
    public static void showToast(Context context, int resId) {
        Toast.makeText(context, context.getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }

    public static float getTextSize(Context context) {
        final int fontSize = Config.Companion.newInstance(context).getFontSize();
        final Resources res = context.getResources();
        float textSize = res.getDimension(R.dimen.medium_text_size);
        switch (fontSize) {
            case Constants.FONT_SIZE_SMALL:
                textSize = res.getDimension(R.dimen.small_text_size);
                break;
            case Constants.FONT_SIZE_LARGE:
                textSize = res.getDimension(R.dimen.large_text_size);
                break;
            case Constants.FONT_SIZE_EXTRA_LARGE:
                textSize = res.getDimension(R.dimen.extra_large_text_size);
                break;
        }
        return textSize;
    }

    public static void updateWidget(Context context) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));

        final Intent intent = new Intent(context, MyWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
