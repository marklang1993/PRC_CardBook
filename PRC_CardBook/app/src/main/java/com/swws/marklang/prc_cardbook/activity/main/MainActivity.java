package com.swws.marklang.prc_cardbook.activity.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.card.CardActivity;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ArrayList<Database> mDatabases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init. views
        setListView();
    }

    private void setListView() {
        // Read meta data
        FileUtility fileUtility = new FileUtility(getApplicationContext());
        mDatabases = fileUtility.ReadAllMetaData(true);

        // Init. seriesListView
        ListView seriesListView = (ListView) findViewById(R.id.seriesListView);
        SeriesItemAdapter seriesItemAdapter = new SeriesItemAdapter(
                getApplicationContext(), mDatabases
        );
        seriesListView.setAdapter(seriesItemAdapter);
        seriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showCardActivity = new Intent(getApplicationContext(), CardActivity.class);
                // Pass params to "CardActivity"
                Database currentDatabase = mDatabases.get(position);
                showCardActivity.putExtra(CardActivity.KEY_SERIES_INDEX, position);
                showCardActivity.putExtra(CardActivity.KEY_DATABASE, currentDatabase);
                // Start
                startActivity(showCardActivity);
            }
        });
    }
}
