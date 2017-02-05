package com.example.android.booklisting;

/**
 * Created by Will on 2/1/2017.
 */

public class Book {
    private String mTitle;
    private String mAuthor;

    public Book(String title, String author) {
        mTitle = title;
        mAuthor = author;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }
}
