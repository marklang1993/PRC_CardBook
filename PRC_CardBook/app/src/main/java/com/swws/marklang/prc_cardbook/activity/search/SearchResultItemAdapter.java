package com.swws.marklang.prc_cardbook.activity.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.ItemEx;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

import java.util.ArrayList;

public class SearchResultItemAdapter extends BaseAdapter {

    private ArrayList<ItemEx> mSearchResult;
    private LayoutInflater mInflater;


    /**
     * Constructor
     * @param searchResult
     * @param context
     */
    public SearchResultItemAdapter(Context context, ArrayList<ItemEx> searchResult) {
        mSearchResult = searchResult;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View resultItemView = mInflater.inflate(R.layout.search_result_listview, null);
        TextView searchResultItemNameTextView = (TextView)
                resultItemView.findViewById(R.id.searchResultItemNameTextView);
        TextView searchResultItemInventoryTextView = (TextView)
                resultItemView.findViewById(R.id.searchResultItemInventoryTextView);

        ItemEx item = mSearchResult.get(position);
        int inventoryCount = InventoryUtility.getInventoryCount(item);
        searchResultItemNameTextView.setText(item.ItemName);
        searchResultItemInventoryTextView.setText(String.valueOf(inventoryCount));
        if (inventoryCount == 0) {
            searchResultItemNameTextView.setTextColor(resultItemView.getResources().getColor(R.color.red));
            searchResultItemInventoryTextView.setTextColor(resultItemView.getResources().getColor(R.color.red));
        }

        return resultItemView;
    }
}
