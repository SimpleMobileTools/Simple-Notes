package com.simplemobiletools.notes;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    public static void showToast(Context context, int resId) {
        Toast.makeText(context, context.getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }
}
