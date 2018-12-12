package com.swws.marklang.prc_cardbook;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;

import java.util.ArrayList;


public class MainActivity extends Activity {

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

    }
}
