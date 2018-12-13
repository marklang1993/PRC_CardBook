package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.Database;

public class CardActivity extends AppCompatActivity {

    public static final String KEY_SERIES_INDEX = "com.swws.marklang.prc_cardbook.SERIES_INDEX";
    public static final String KEY_DATABASE = "com.swws.marklang.prc_cardbook.DATABASE";

    private static final int H_COUNT = 4; // count of cards displayed in horizontal direction
    private static final int V_COUNT = 5; // count of cards displayed in vertical direction
    private static final int OFFSET = 10; // offset between 2 cards

    private int mSeriesIndex = 0;
    private Database mDatabase = null;

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
        }
        if (intent.hasExtra(KEY_DATABASE)) {
            mDatabase = (Database) intent.getExtras().getParcelable(KEY_DATABASE);
        } else {
            Log.e(this.getClass().getName(), KEY_DATABASE + " NOT FOUND!");
        }

        // Set title
        setTitle(mDatabase.name() + "  " + getString(R.string.card_activity_name));

        // Set cardGridView
        GridView cardGridView = (GridView) findViewById(R.id.cardGridView);
        cardGridView.setAdapter(new CardItemAdapter(getApplicationContext(), mDatabase, getResources()));
    }

}
