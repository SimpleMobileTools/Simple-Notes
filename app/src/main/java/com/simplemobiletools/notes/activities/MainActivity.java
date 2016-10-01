package com.simplemobiletools.notes.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.simplemobiletools.notes.R;
import com.simplemobiletools.notes.Utils;
import com.simplemobiletools.notes.databases.DBHelper;
import com.simplemobiletools.notes.models.Note;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends SimpleActivity {
    @BindView(R.id.notes_view) EditText mNotesView;

    private DBHelper mDb;
    private Note mCurrentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDb = DBHelper.newInstance(getApplicationContext());
        mCurrentNote = mDb.getGeneralNote();
        mNotesView.setText(mCurrentNote.getValue());
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        mNotesView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getTextSize(getApplicationContext()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConfig.setIsFirstRun(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    @OnClick(R.id.notes_fab)
    public void fabClicked(View view) {
        final View newNoteView = getLayoutInflater().inflate(R.layout.new_note, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.new_note));
        builder.setView(newNoteView);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText titleET = (EditText) newNoteView.findViewById(R.id.note_name);
                final String title = titleET.getText().toString().trim();
                if (title.isEmpty()) {
                    Utils.showToast(getApplicationContext(), R.string.no_title);
                } else if (mDb.doesTitleExist(title)) {
                    Utils.showToast(getApplicationContext(), R.string.title_taken);
                } else {
                    alertDialog.dismiss();
                }
            }
        });
    }

    private void saveText() {
        final String newText = getCurrentNote();
        final String oldText = mCurrentNote.getValue();
        if (!newText.equals(oldText)) {
            Utils.showToast(getApplicationContext(), R.string.note_saved);
            mCurrentNote.setValue(newText);
            mDb.updateNote(mCurrentNote);
        }

        hideKeyboard();
        Utils.updateWidget(getApplicationContext());
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

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNotesView.getWindowToken(), 0);
    }
}
