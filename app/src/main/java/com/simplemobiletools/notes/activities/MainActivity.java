package com.simplemobiletools.notes.activities;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

import com.simplemobiletools.notes.R;
import com.simplemobiletools.notes.Utils;
import com.simplemobiletools.notes.databases.DBHelper;
import com.simplemobiletools.notes.models.Note;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends SimpleActivity {
    @BindView(R.id.notes_view) EditText mNotesView;
    @BindView(R.id.current_note_label) TextView mCurrNoteLabel;
    @BindView(R.id.current_note_title) TextView mCurrNoteTitle;

    private DBHelper mDb;
    private Note mCurrentNote;
    private List<Note> mNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDb = DBHelper.newInstance(getApplicationContext());
        mNotes = mDb.getNotes();
        updateSelectedNote(mConfig.getCurrentNoteIndex());
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
        final MenuItem openNote = menu.findItem(R.id.open_note);
        openNote.setVisible(mNotes.size() > 1);

        final MenuItem deleteNote = menu.findItem(R.id.delete_note);
        deleteNote.setVisible(mNotes.size() > 1);

        final MenuItem changeNote = menu.findItem(R.id.change_widget_note);
        changeNote.setVisible(mNotes.size() > 1);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_note:
                displayDeleteNotePrompt();
                return true;
            case R.id.open_note:
                displayOpenNoteDialog();
                return true;
            case R.id.share:
                shareText();
                return true;
            case R.id.change_widget_note:
                showWidgetNotePicker();
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

    private void showWidgetNotePicker() {

    }

    private void updateSelectedNote(int index) {
        saveText();
        mConfig.setCurrentNoteIndex(index);
        mCurrentNote = mNotes.get(index);
        mNotesView.setText(mCurrentNote.getValue());
        mCurrNoteTitle.setText(mCurrentNote.getTitle());

        mCurrNoteLabel.setVisibility(mNotes.size() <= 1 ? View.GONE : View.VISIBLE);
        mCurrNoteTitle.setVisibility(mNotes.size() <= 1 ? View.GONE : View.VISIBLE);
        Utils.updateWidget(getApplicationContext());
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
                    saveText();
                    final Note newNote = new Note(0, title, "");
                    final int id = mDb.insertNote(newNote);
                    newNote.setId(id);
                    mNotes = mDb.getNotes();

                    final int newNoteIndex = getNewNoteIndex(newNote);
                    updateSelectedNote(newNoteIndex);
                    alertDialog.dismiss();
                    invalidateOptionsMenu();
                }
            }
        });
    }

    private int getNewNoteIndex(Note note) {
        final int cnt = mNotes.size();
        int index = 0;
        for (int i = 0; i < cnt; i++) {
            if (mNotes.get(i).equals(note)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void displayDeleteNotePrompt() {
        final Resources res = getResources();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.delete_note_prompt_title));
        builder.setMessage(String.format(res.getString(R.string.delete_note_prompt_message), mCurrentNote.getTitle()));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNote();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void deleteNote() {
        if (mNotes.size() <= 1)
            return;

        mDb.deleteNote(mCurrentNote.getId());
        mNotes = mDb.getNotes();
        updateSelectedNote(0);
        invalidateOptionsMenu();
    }

    private void displayOpenNoteDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.pick_a_note));

        final int cnt = mNotes.size();
        String[] notes = new String[cnt];
        for (int i = 0; i < cnt; i++) {
            notes[i] = mNotes.get(i).getTitle();
        }

        builder.setItems(notes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateSelectedNote(which);
            }
        });
        builder.show();
    }

    private void saveText() {
        if (mCurrentNote == null)
            return;

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
