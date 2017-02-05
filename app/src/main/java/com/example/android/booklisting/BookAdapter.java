package com.example.android.booklisting;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Will on 2/1/2017.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<Book> mBooks;

    public BookAdapter(Context context, int layoutResourceId, ArrayList<Book> books) {
        super(context, layoutResourceId, books);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mBooks = books;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item, null);

        ViewHolder viewHolder = new ViewHolder();

        viewHolder.title = (TextView) view.findViewById(R.id.title);
        viewHolder.author = (TextView) view.findViewById(R.id.author);

        viewHolder.title.setText(mBooks.get(position).getTitle());
        viewHolder.author.setText(mBooks.get(position).getAuthor());

        return view;
    }

    static class ViewHolder {
        TextView title;
        TextView author;
    }
}
