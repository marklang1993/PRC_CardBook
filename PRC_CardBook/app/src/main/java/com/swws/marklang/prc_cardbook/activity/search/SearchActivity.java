package com.swws.marklang.prc_cardbook.activity.search;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.ItemEx;

import java.util.ArrayList;
import java.util.LinkedList;

public class SearchActivity extends AppCompatActivity {

    private SearchTask mSearchTask;
    private static ArrayList<ItemEx> mResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initUIs();

        mSearchTask = null;
    }

    /**
     * Notify the result
     * @param result
     */
    public void notify(ArrayList<ItemEx> result) {
        mSearchTask = null;
        mResult = result;

        // Display the "Search" button again
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setVisibility(View.VISIBLE);

        // Display search result
        if (result.size() > 0) {
            Intent startSearchResultActivityIntent = new Intent(SearchActivity.this, SearchResultActivity.class);
            startActivity(startSearchResultActivityIntent);
        }
    }

    /**
     * Get last search result
     * @return
     */
    public static ArrayList<ItemEx> getSearchResult() {
        return mResult;
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        // If the current search task is running, stop it
        if (mSearchTask != null){
            mSearchTask.cancel(true);
            mSearchTask = null;
        }

        // Close this activity
        finish();
        return true;
    }

    /**
     * Init. all UIs
     */
    private void initUIs(){
        setTitle(R.string.search_activity_name);

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable

        // Search button
        final Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input items' name
                EditText itemName1EditText = (EditText) findViewById(R.id.itemName1EditText);
                EditText itemName2EditText = (EditText) findViewById(R.id.itemName2EditText);
                EditText itemName3EditText = (EditText) findViewById(R.id.itemName3EditText);
                EditText itemName4EditText = (EditText) findViewById(R.id.itemName4EditText);
                String inputItemName1 = itemName1EditText.getText().toString();
                String inputItemName2 = itemName2EditText.getText().toString();
                String inputItemName3 = itemName3EditText.getText().toString();
                String inputItemName4 = itemName4EditText.getText().toString();

                LinkedList<String> inputItemNames = new LinkedList<>();
                if (!inputItemName1.equals("")) {
                    inputItemNames.add(inputItemName1);
                }
                if (!inputItemName2.equals("")) {
                    inputItemNames.add(inputItemName2);
                }
                if (!inputItemName3.equals("")) {
                    inputItemNames.add(inputItemName3);
                }
                if (!inputItemName4.equals("")) {
                    inputItemNames.add(inputItemName4);
                }

                // inputItemNames should not be empty
                if (inputItemNames.size() > 0) {
                    searchButton.setVisibility(View.INVISIBLE);
                    // Start search task
                    mSearchTask = new SearchTask(SearchActivity.this, inputItemNames);
                    mSearchTask.execute();

                }
            }
        });
    }
}
