package com.example.android.booklisting;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String URL = "https://www.googleapis.com/books/v1/volumes";
    private final static String LOG_TAG = "MainActivity";

    private ListView mBookList;
    private TextView mEmptyStateTextView;
    private ProgressBar mLoadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookList = (ListView) findViewById(R.id.book_list);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        mLoadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            handleIntent(getIntent());
        } else {
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mEmptyStateTextView.setVisibility(View.GONE);
            mLoadingSpinner.setVisibility(View.VISIBLE);

            String query = intent.getStringExtra(SearchManager.QUERY);

            String[] queryParse = query.split(" ");
            String url = URL + "?q=";

            for (int j = 0; j < queryParse.length; j++) {
                url += queryParse[j];

                if (j < queryParse.length-1) {
                    url += "+";
                }
            }
            new BookAsyncTask().execute(url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    public class BookAsyncTask extends AsyncTask<String, Void, ArrayList<Book>> {

        @Override
        protected ArrayList<Book> doInBackground(String... params) {
            java.net.URL url = createUrl(params[0]);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<Book> books = extractBooksFromJson(jsonResponse);

            return books;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> result) {
            mLoadingSpinner.setVisibility(View.GONE);

            if (result.size() == 0) {
                mEmptyStateTextView.setText(getString(R.string.no_books_to_show));
                mEmptyStateTextView.setVisibility(View.VISIBLE);
            }

            BookAdapter bookAdapter = new BookAdapter(MainActivity.this, R.layout.list_item, result);
            mBookList.setAdapter(bookAdapter);
        }
    }

    private ArrayList<Book> extractBooksFromJson(String jsonResponse) {
        ArrayList<Book> books = new ArrayList<Book>();
        String title = "";
        String authors = "";

        try {
            JSONArray items = new JSONObject(jsonResponse).getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject book = items.getJSONObject(i).getJSONObject("volumeInfo");
                title = book.getString("title");
                authors = "";

                if (book.has("authors")) {

                    for (int j = 0; j < book.getJSONArray("authors").length(); j++) {
                        authors += book.getJSONArray("authors").get(j);

                        if (j < book.getJSONArray("authors").length() - 1) {
                            authors += ", ";
                        }
                    }
                }

                books.add(new Book(title, authors));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return books;
    }

    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
            return null;
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
