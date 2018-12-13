package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.Size;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;

import org.w3c.dom.Text;


public class CardItemAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private Database mDatabase;

    public CardItemAdapter(Context context, Database database) {
        mDatabase = database;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
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

        ImageView cardImageView = (ImageView) view.findViewById(R.id.cardImageView);
        TextView cardIdTextView = (TextView) view.findViewById(R.id.cardIdTextView);
        cardIdTextView.setText(mDatabase.get(position).InternalID);

        setImageByScaling(cardImageView, mDatabase.get(position));
        return view;
    }

    private void setImageByScaling(ImageView iv, Item cardItem) {
        // Get card image
        FileUtility fileUtility = new FileUtility(mContext);
        Bitmap cardImage = fileUtility.ReadCardImage(cardItem);

        // Scale and set image
        iv.setScaleType(ImageView.ScaleType.CENTER);
        // TODO: size
        iv.setImageBitmap(
                Bitmap.createScaledBitmap(
                        cardImage,
                        300,
                        300,
                        false
                )
        );

    }
}
