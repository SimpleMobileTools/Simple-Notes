package com.simplemobiletools.notes.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.simplemobiletools.notes.Config;
import com.simplemobiletools.notes.Constants;
import com.simplemobiletools.notes.MyWidgetProvider;
import com.simplemobiletools.notes.R;
import com.simplemobiletools.notes.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends SimpleActivity {
    @BindView(R.id.notes_view) EditText mNotesView;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPrefs = getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        mNotesView.setText(getSavedNote());
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Config.newInstance(getApplicationContext()).getIsAutosaveEnabled()) {
            saveText(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mConfig.getShouldPromptAutosave() && !getCurrentNote().equals(getSavedNote())) {
            mConfig.setShouldPromptAutosave(false);
            displayAutosavePrompt();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Config.newInstance(getApplicationContext()).setIsFirstRun(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (Config.newInstance(getApplicationContext()).getIsAutosaveEnabled())
            menu.findItem(R.id.save).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveText(true);
                return true;
            case R.id.share:
                shareText();
                return true;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayAutosavePrompt() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.unsaved_changes));
        alertDialog.setMessage(getString(R.string.autosave_prompt_msg));

        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.setPositiveButton(R.string.enable_autosave, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.create().show();
    }

    private void saveText(boolean showToast) {
        final String text = getCurrentNote();
        mPrefs.edit().putString(Constants.TEXT, text).apply();

        if (showToast) {
            Utils.showToast(getApplicationContext(), R.string.text_saved);
        }

        hideKeyboard();
        updateWidget();
    }

    private void shareText() {
        final String text = getCurrentNote();
        if (text.isEmpty()) {
            Utils.showToast(getApplicationContext(), R.string.cannot_share_empty_text);
            return;
        }

        final Resources res = getResources();
        final String shareTitle = res.getString(R.string.share_via);
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.simple_note));
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, shareTitle));
    }

    private String getCurrentNote() {
        return mNotesView.getText().toString().trim();
    }

    private String getSavedNote() {
        return mPrefs.getString(Constants.TEXT, "");
    }

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNotesView.getWindowToken(), 0);
    }

    private void updateWidget() {
        final Context context = getApplicationContext();
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, MyWidgetProvider.class));

        final Intent intent = new Intent(this, MyWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
