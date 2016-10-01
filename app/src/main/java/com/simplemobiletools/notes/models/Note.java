package com.simplemobiletools.notes.models;

public class Note {
    private int mId;
    private String mTitle;
    private String mValue;

    public Note(int id, String title, String value) {
        mId = id;
        mTitle = title;
        mValue = value;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public String toString() {
        return "Note {" +
                "id=" + getId() +
                ", title=" + getTitle() +
                ", value=" + getValue() +
                "}";
    }
}
