package com.simplemobiletools.notes.databases;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.simplemobiletools.notes.Constants;
import com.simplemobiletools.notes.models.Note;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "notes";
    private static final String NOTE = "General note";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_TEXT = "value";

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
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_TEXT + " TEXT" +
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
        final Note note = new Note(0, NOTE, text);
        insertNote(note, db);
    }

    private void insertNote(Note note, SQLiteDatabase db) {
        final ContentValues values = fillContentValues(note);
        db.insert(TABLE, null, values);
    }

    public void insertNote(Note note) {
        final ContentValues values = fillContentValues(note);
        mDb.insert(TABLE, null, values);
    }

    private ContentValues fillContentValues(Note note) {
        final ContentValues values = new ContentValues();
        values.put(COL_NAME, note.getName());
        values.put(COL_TEXT, note.getText());
        return values;
    }
}
