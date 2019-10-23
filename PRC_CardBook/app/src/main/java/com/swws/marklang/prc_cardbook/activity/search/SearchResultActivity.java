package com.swws.marklang.prc_cardbook.activity.search;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.activity.card.CardDetailActivity;
import com.swws.marklang.prc_cardbook.utility.database.ItemEx;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {

    private SearchResultItemAdapter mSearchResultItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // Set title
        setTitle(R.string.search_activity_name);

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable

        // Set ListView
        final ArrayList<ItemEx> searchResult = SearchActivity.getSearchResult();
        ListView searchResultListView = (ListView) findViewById(R.id.searchResultListView);
        mSearchResultItemAdapter= new SearchResultItemAdapter(this, searchResult);
        searchResultListView.setAdapter(mSearchResultItemAdapter);

        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showCardDetailActivity = new Intent(getApplicationContext(), CardDetailActivity.class);
                ItemEx item = searchResult.get(position);

                // Passing params
                showCardDetailActivity.putExtra(CardDetailActivity.KEY_CARD_DETAIL_START_TYPE, CardDetailActivity.StartType.SEARCH.toString());
                showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_INDEX, position);
                showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_SEASON_ID, item.mSeasonID.toString());

                // Start the CardDetailActivity
                startActivityForResult(showCardDetailActivity, Constants.REQUEST_AR_CARD_INVENTORY_CHANGE);
            }
        });
    }

    /**
     * Notify "searchResultListView" to update once the CardDetailActivity finished.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_AR_CARD_INVENTORY_CHANGE) {
            if (resultCode == RESULT_OK) {
                // Update "searchResultListView" if the inventory of any item changed
                mSearchResultItemAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Close this activity
        finish();
        return true;
    }
}
