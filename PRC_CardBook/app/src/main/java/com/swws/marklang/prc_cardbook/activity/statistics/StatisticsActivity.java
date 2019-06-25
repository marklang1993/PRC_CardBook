package com.swws.marklang.prc_cardbook.activity.statistics;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainActivity;


public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Init. UI
        setTitle(R.string.statistics_activity_name);
        initButtons();
        setListView();
    }

    /**
     * Init. all buttons in this activity
     */
    public void initButtons() {
        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
    }

    /**
     * Set up "seriesListView"
     */
    private void setListView() {
        // Init. statisticsSeriesListView
        ListView statisticsSeriesListView = (ListView) findViewById(R.id.statisticsSeriesListView);

        SeriesItemAdapter seriesItemAdapter = new SeriesItemAdapter(
                getApplicationContext(),
                MainActivity.getAllDatabases()
        );
        statisticsSeriesListView.setAdapter(seriesItemAdapter);
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
