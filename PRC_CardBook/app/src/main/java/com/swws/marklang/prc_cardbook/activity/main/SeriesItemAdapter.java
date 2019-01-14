package com.swws.marklang.prc_cardbook.activity.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.Database;

import java.util.ArrayList;

public class SeriesItemAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<String> mSeriesNames;


    public SeriesItemAdapter(Context context, ArrayList<Database> databases)
    {
        initSeriesNames(databases);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View view = mInflater.inflate(R.layout.series_listview, null);
        TextView seriesNameTextView = (TextView) view.findViewById(R.id.seriesNameTextView);

        seriesNameTextView.setText(mSeriesNames.get(position));

        return view;
    }

    /**
     * Update the Adapter and UI components
     * @param newDatabases
     */
    public void notifyDataSetChanged(ArrayList<Database> newDatabases) {
        // Update internal databases
        initSeriesNames(newDatabases);

        // Go to update
        super.notifyDataSetChanged();
    }

    /**
     * Init. seriesNames
     */
    private void initSeriesNames(ArrayList<Database> databases) {
        // Init. seriesNames
        if (databases != null) {
            mSeriesNames = new ArrayList<>(databases.size());
            for (Database d : databases)
            {
                mSeriesNames.add(d.name());
            }

        } else {
            mSeriesNames = new ArrayList<>();
        }
    }
}
