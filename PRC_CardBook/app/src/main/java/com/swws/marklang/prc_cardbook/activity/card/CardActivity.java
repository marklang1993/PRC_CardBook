package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;

public class CardActivity extends AppCompatActivity {

    public static final String KEY_SERIES_INDEX = "com.swws.marklang.prc_cardbook.SERIES_INDEX";
    public static final String KEY_DATABASE = "com.swws.marklang.prc_cardbook.DATABASE";

    private int mSeriesIndex = 0;
    private Database mDatabase = null;

    private CardItemAdapter mCardItemAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // Extract passed in info.
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_SERIES_INDEX)) {
            mSeriesIndex = intent.getExtras().getInt(KEY_SERIES_INDEX);
        } else {
            Log.e(this.getClass().getName(), KEY_SERIES_INDEX + " NOT FOUND!");
            return;
        }
        if (intent.hasExtra(KEY_DATABASE)) {
            mDatabase = (Database) intent.getExtras().getParcelable(KEY_DATABASE);
        } else {
            Log.e(this.getClass().getName(), KEY_DATABASE + " NOT FOUND!");
            return;
        }

        // Set title
        setTitle(mDatabase.name());

        // Set cardGridView
        GridView cardGridView = (GridView) findViewById(R.id.cardGridView);
        mCardItemAdapter = new CardItemAdapter(getApplicationContext(), mDatabase, getResources());
        cardGridView.setAdapter(mCardItemAdapter);

        // Set onItemClickListener
        cardGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showCardDetailActivity = new Intent(getApplicationContext(), CardDetailActivity.class);
                Item cardItem = mDatabase.get(position);

                // Passing the item of the card
                showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM, cardItem);

                // Start the CardDetailActivity
                startActivityForResult(showCardDetailActivity, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Update "cardGridView" after "CardDetailActivity" closed.
        mCardItemAdapter.notifyDataSetChanged();
    }

}
