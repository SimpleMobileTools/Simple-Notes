package com.simplemobiletools.notes.models;

public class Note {
    private int mId;
    private String mName;
    private String mText;

    public Note(int id, String name, String text) {
        mId = id;
        mName = name;
        mText = text;
    }

    public int getmId() {
        return mId;
    }

    public String getmName() {
        return mName;
    }

    public String getmText() {
        return mText;
    }
}
