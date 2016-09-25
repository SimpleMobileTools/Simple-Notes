package com.simplemobiletools.notes;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

public class Utils {
    public static void showToast(Context context, int resId) {
        Toast.makeText(context, context.getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }

    public static float getTextSize(Context context) {
        final int fontSize = Config.newInstance(context).getFontSize();
        final Resources res = context.getResources();
        float textSize = res.getDimension(R.dimen.medium_text_size);
        switch (fontSize) {
            case Constants.FONT_SIZE_SMALL:
                textSize = res.getDimension(R.dimen.small_text_size);
                break;
            case Constants.FONT_SIZE_LARGE:
                textSize = res.getDimension(R.dimen.large_text_size);
                break;
        }
        return textSize;
    }
}
