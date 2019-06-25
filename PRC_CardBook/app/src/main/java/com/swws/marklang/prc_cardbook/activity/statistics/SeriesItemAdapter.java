package com.swws.marklang.prc_cardbook.activity.statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainLoadActivity;
import com.swws.marklang.prc_cardbook.utility.database.Database;

import java.util.ArrayList;

public class SeriesItemAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<String> mSeriesNames;
    private ArrayList<String> mSeriesCounts;

    private int mCountTaskFinished;


    public SeriesItemAdapter(Context context, ArrayList<Database> databases)
    {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCountTaskFinished = 0;

        initSeriesData(databases);
    }

    @Override
    public int getCount() {
        return mSeriesNames.size();
    }

    @Override
    public Object getItem(int position) {
        return mSeriesNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.statistics_series_listview, null);
        TextView statisticsSeriesNameTextView = (TextView) view.findViewById(R.id.statisticsSeriesNameTextView);
        TextView statisticsSeriesCountTextView = (TextView) view.findViewById(R.id.statisticsSeriesCountTextView);

        statisticsSeriesNameTextView.setText(mSeriesNames.get(position));
        statisticsSeriesCountTextView.setText(mSeriesCounts.get(position));

        return view;
    }

    /**
     * Notify this adapter the calculation is done
     * @param position
     */
    public void notifyCalculationFinished(int position, String result) {
        // NOTE: this function is called from UI thread by onPostExecute(), so it is thread-safe.
        ++mCountTaskFinished;
        mSeriesCounts.set(position, result);

        if (mCountTaskFinished == mSeriesCounts.size()) {
            // All calculations completed
            mCountTaskFinished = 0;
            super.notifyDataSetChanged();
        }
    }

    /**
     * Update the Adapter and UI components
     * @param newDatabases
     */
    public void notifyDataSetChanged(ArrayList<Database> newDatabases) {
        // Update internal databases
        initSeriesData(newDatabases);

        // Go to update
        super.notifyDataSetChanged();
    }

    /**
     * Init. initSeriesData
     */
    private void initSeriesData(ArrayList<Database> databases) {
        // Init. mSeriesNames & mSeriesCounts
        if (databases != null) {
            mSeriesNames = new ArrayList<>(databases.size());
            mSeriesCounts = new ArrayList<>(databases.size());

            int position = 0;
            for (Database d : databases) {
                mSeriesNames.add(d.name());
                mSeriesCounts.add(
                        MainLoadActivity.getCurrentApplicationContext().
                                getString(R.string.lable_calculating_text)
                );

                // Do calculation in background thread
                CalculateTask calculateTask = new CalculateTask(this, position);
                calculateTask.execute();

                ++position;
            }

        } else {
            mSeriesNames = new ArrayList<>();
            mSeriesCounts = new ArrayList<>();
        }
    }
}

