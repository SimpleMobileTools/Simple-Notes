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

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getText() {
        return mText;
    }
}
