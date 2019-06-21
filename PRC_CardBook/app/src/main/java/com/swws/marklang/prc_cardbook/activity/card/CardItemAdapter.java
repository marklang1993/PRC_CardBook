package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidessence.lib.RichTextView;
import com.google.zxing.common.StringUtils;
import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;


public class CardItemAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private Database mDatabase;
    private Resources mRes;

    // All JR colors
    private int[] mJRColors;

    public CardItemAdapter(Context context, Database database, Resources res) {
        mDatabase = database;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mRes = res;

        // Init. JR colors
        mJRColors = new int[CardDetailActivity.JR_COLOR_TOTAL_COUNT];
        mJRColors[0] = ContextCompat.getColor(mContext, R.color.JR_pink);
        mJRColors[1] = ContextCompat.getColor(mContext, R.color.JR_yellow);
        mJRColors[2] = ContextCompat.getColor(mContext, R.color.JR_blue);
        mJRColors[3] = ContextCompat.getColor(mContext, R.color.JR_red);
        mJRColors[4] = ContextCompat.getColor(mContext, R.color.JR_green);
        mJRColors[5] = ContextCompat.getColor(mContext, R.color.JR_purple);
        mJRColors[6] = ContextCompat.getColor(mContext, R.color.JR_black);
        mJRColors[7] = ContextCompat.getColor(mContext, R.color.JR_gold);
    }

    @Override
    public int getCount() {
        return mDatabase.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatabase.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.card_gridview, null);

        SeasonID seasonID = mDatabase.seasonId();
        Item item = mDatabase.get(position);

        // Display item internal ID
        TextView cardIdTextView = (TextView) view.findViewById(R.id.cardIdTextView);
        cardIdTextView.setText(item.InternalID);

        // Load other time-consuming resources and display
        CardItemLoadTask loadTask = new CardItemLoadTask(mRes, view, seasonID, item, mJRColors);
        loadTask.execute();

        return view;
    }



}
