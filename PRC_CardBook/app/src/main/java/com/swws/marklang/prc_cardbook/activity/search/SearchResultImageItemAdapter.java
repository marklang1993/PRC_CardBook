package com.swws.marklang.prc_cardbook.activity.search;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.card.CardItemLoadTask;
import com.swws.marklang.prc_cardbook.activity.setting.SettingFileUtility;
import com.swws.marklang.prc_cardbook.utility.concurrent.ConcurrentTaskController;
import com.swws.marklang.prc_cardbook.utility.database.ItemEx;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

public class SearchResultImageItemAdapter extends BaseAdapter {

    private static final int MAX_TASK_COUNT = 8;

    private ArrayList<ItemEx> mSearchResult;
    private LayoutInflater mInflater;
    private Resources mRes;
    private boolean mIsGreyLevel;

    private ConcurrentTaskController mAsyncTaskController;

    /**
     * Constructor
     * @param searchResult
     * @param context
     */
    public SearchResultImageItemAdapter(Context context, ArrayList<ItemEx> searchResult, Resources res) {
        mSearchResult = searchResult;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRes = res;

        SettingFileUtility settingFileUtility = SettingFileUtility.getInstance();
        mIsGreyLevel = settingFileUtility.getBooleanValue(
                settingFileUtility.readItem("card_not_possessed_without_color"));
        mAsyncTaskController = new ConcurrentTaskController(MAX_TASK_COUNT);
    }

    @Override
    public int getCount() {
        return mSearchResult.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchResult.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.card_gridview, null);

        ItemEx itemEx = mSearchResult.get(position);
        SeasonID seasonID = itemEx.mSeasonID;

        // Display item internal ID
        TextView cardIdTextView = (TextView) view.findViewById(R.id.cardIdTextView);
        cardIdTextView.setText(itemEx.InternalID);

        // Load other time-consuming resources and display
        CardItemLoadTask loadTask = new CardItemLoadTask(
                mRes,
                view,
                seasonID,
                itemEx,
                null,
                mIsGreyLevel,
                mAsyncTaskController
        );
        mAsyncTaskController.executeTask(loadTask);

        return view;
    }
}
