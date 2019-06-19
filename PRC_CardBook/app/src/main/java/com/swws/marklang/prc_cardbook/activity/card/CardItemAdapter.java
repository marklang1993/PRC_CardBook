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

    private static final float SIZE_SP = 100.0f; // TODO: size

    private Context mContext;
    private LayoutInflater mInflater;
    private Database mDatabase;
    private Resources mRes;

    // Colors from R
    private int mColorRed;
    private int mColorGreen;
    private int mColorBlue;

    private int[] mJRColors;

    public CardItemAdapter(Context context, Database database, Resources res) {
        mDatabase = database;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mRes = res;

        // Init. colors
        mColorRed = ContextCompat.getColor(mContext, R.color.red);
        mColorGreen = ContextCompat.getColor(mContext, R.color.green);
        mColorBlue = ContextCompat.getColor(mContext, R.color.blue);

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
        // TODO: make the scrolling smoother
        View view = mInflater.inflate(R.layout.card_gridview, null);

        // Retrieve the UI objects
        ImageView cardImageView = (ImageView) view.findViewById(R.id.cardImageView);
        TextView cardIdTextView = (TextView) view.findViewById(R.id.cardIdTextView);
        RichTextView inventoryCountTextView = (RichTextView) view.findViewById(R.id.inventoryCountTextView);

        // Set the UI objects
        cardIdTextView.setText(mDatabase.get(position).InternalID);
        setCardImageByScaling(cardImageView, mDatabase.seasonId(), mDatabase.get(position));
        setCardInventoryCount(inventoryCountTextView, mDatabase.seasonId(), mDatabase.get(position));

        return view;
    }


    /**
     * Set the inventory count of an item
     * @param tv
     * @param seasonID
     * @param cardItem
     */
    private void setCardInventoryCount(RichTextView tv, SeasonID seasonID, Item cardItem) {

        // Get item inventory count
        int countCardInventory = InventoryUtility.getInventoryCount(seasonID, cardItem);

        if (!cardItem.Rarity.equals("JR")) {
            // 1. Not a JR item

            // Set text color
            if (countCardInventory == 0)
            {
                // No inventory - RED
                tv.setTextColor(mColorRed);

            } else if (countCardInventory == 1) {
                // Only 1 - GREEN
                tv.setTextColor(mColorGreen);

            } else {
                // More than 1 - BLUE
                tv.setTextColor(mColorBlue);
            }

            // Set text of inventory count
            tv.setText(String.valueOf(countCardInventory));

        } else {
            // 2. JR item
            String starString = "★";
            String targetString = String.format("%0" + CardDetailActivity.JR_COLOR_TOTAL_COUNT + "d", 0)
                    .replace("0", starString);
            tv.setText(targetString);
            
            // Set displayed color based on the corresponding item color
            for (int i = 0; i < CardDetailActivity.JR_COLOR_TOTAL_COUNT; ++i) {
                int shiftBitCount = 3 * i;
                int currentJRItemInventory = (countCardInventory >>> shiftBitCount)
                        & CardDetailActivity.MAX_JR_INVENTORY_COUNT;
                // Check the inventory
                if (currentJRItemInventory > 0) {
                    tv.colorSpan(i, i + 1, RichTextView.ColorFormatType.FOREGROUND, mJRColors[i]);
                }
            }
        }

    }

    /**
     * Scale the image and load it to ImageView iv
     * @param iv
     * @param seasonID
     * @param cardItem
     */
    private void setCardImageByScaling(ImageView iv, SeasonID seasonID, Item cardItem) {
        // Get card image
        FileUtility fileUtility = new FileUtility(mContext);
        Bitmap cardImage = fileUtility.ReadImage(
                cardItem.ItemImage,
                FileUtility.IMAGE_TYPE.IMAGE,
                seasonID
        );
        // Get the count of given card inventory
        int countCardInventory = InventoryUtility.getInventoryCount(seasonID, cardItem);

        // Check is cardImage NULL
        if (cardImage == null)
        {
            return;
        }

        // Change image color by inventory count
        if (countCardInventory == 0)
        {
            // If the user does not own this card, show the card image in greyscale
            cardImage = bitmapToGreyscale(cardImage);
        }

        // Scale
        float side = SIZE_SP * mRes.getDisplayMetrics().scaledDensity;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(cardImage, (int)side, (int)side, false);

        // Display card image
        iv.setScaleType(ImageView.ScaleType.CENTER);
        iv.setImageBitmap(scaledBitmap);

    }

    /**
     * Change a bitmap with color to a bitmap in greyscale
     * @param srcBitmap
     * @return
     */
    private Bitmap bitmapToGreyscale(Bitmap srcBitmap) {
        // Create an blank grey bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                srcBitmap.getWidth(),
                srcBitmap.getHeight(),
                Bitmap.Config.ARGB_8888 // NOTE: cannot use RGB565, otherwise we lose the Alpha channel
        );

        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(srcBitmap, 0, 0, paint);

        return dstBitmap;
    }
}
