package com.simplemobiletools.notes.databases;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.simplemobiletools.notes.Constants;
import com.simplemobiletools.notes.models.Note;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "notes";
    private static final String NOTE = "General note";

    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_VALUE = "value";

    private Context mContext;
    private SQLiteDatabase mDb;

    public static DBHelper newInstance(Context context) {
        return new DBHelper(context);
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
        mDb = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_TITLE + " TEXT UNIQUE, " +
                COL_VALUE + " TEXT" +
                ")"
        );

        insertFirstNote(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void insertFirstNote(SQLiteDatabase db) {
        final SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        final String text = prefs.getString(Constants.TEXT, "");
        final Note note = new Note(1, NOTE, text);
        insertNote(note, db);
    }

    private void insertNote(Note note, SQLiteDatabase db) {
        final ContentValues values = fillContentValues(note);
        db.insert(TABLE_NAME, null, values);
    }

    public int insertNote(Note note) {
        final ContentValues values = fillContentValues(note);
        return (int) mDb.insert(TABLE_NAME, null, values);
    }

    private ContentValues fillContentValues(Note note) {
        final ContentValues values = new ContentValues();
        values.put(COL_TITLE, note.getTitle());
        values.put(COL_VALUE, note.getValue());
        return values;
    }

    public void deleteNote(int id) {
        mDb.delete(TABLE_NAME, COL_ID + " = " + id, null);
    }

    public boolean doesTitleExist(String title) {
        final String cols[] = {COL_ID};
        final String selection = COL_TITLE + " = ?";
        final String selectionArgs[] = {title};
        final Cursor cursor = mDb.query(TABLE_NAME, cols, selection, selectionArgs, null, null, null);

        if (cursor == null)
            return false;

        final int cnt = cursor.getCount();
        cursor.close();
        return cnt == 1;
    }

    public List<Note> getNotes() {
        final List<Note> notes = new ArrayList<>();
        final String cols[] = {COL_ID, COL_TITLE, COL_VALUE};
        final Cursor cursor = mDb.query(TABLE_NAME, cols, null, null, null, null, COL_TITLE + " COLLATE NOCASE ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    final int id = cursor.getInt(cursor.getColumnIndex(COL_ID));
                    final String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                    final String value = cursor.getString(cursor.getColumnIndex(COL_VALUE));
                    final Note note = new Note(id, title, value);
                    notes.add(note);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return notes;
    }

    public Note getNote(int id) {
        final String cols[] = {COL_TITLE, COL_VALUE};
        final String selection = COL_ID + " = ?";
        final String selectionArgs[] = {String.valueOf(id)};
        final Cursor cursor = mDb.query(TABLE_NAME, cols, selection, selectionArgs, null, null, null);
        Note note = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final String title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
                final String value = cursor.getString(cursor.getColumnIndex(COL_VALUE));
                note = new Note(id, title, value);
            }
            cursor.close();
        }
        return note;
    }

    public void updateNote(Note note) {
        final ContentValues values = fillContentValues(note);
        final String selection = COL_ID + " = ?";
        final String selectionArgs[] = new String[]{String.valueOf(note.getId())};
        mDb.update(TABLE_NAME, values, selection, selectionArgs);
    }
}
