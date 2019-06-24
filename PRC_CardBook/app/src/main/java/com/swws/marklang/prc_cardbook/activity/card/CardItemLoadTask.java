package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.androidessence.lib.RichTextView;
import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseFileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;


public class CardItemLoadTask extends AsyncTask<Void, Void, CardItemLoadResult> {

    private static final float SIZE_SP = 120.0f; // TODO: size

    private Context mContext;
    private Resources mRes;
    private SeasonID mSeasonID;
    private Item mItem;

    private ImageView mCardImageView;
    private RichTextView mInventoryCountTextView;

    // Colors from R
    private int mColorRed;
    private int mColorGreen;
    private int mColorBlue;

    private int[] mJRColors;

    public CardItemLoadTask(
            Resources res,
            View view,
            SeasonID seasonID,
            Item item,
            int[] JRColors) {

        // Init. data
        mContext = view.getContext();
        mRes = res;
        mSeasonID = seasonID;
        mItem = item;

        // Init. colors
        mColorRed = ContextCompat.getColor(mContext, R.color.red);
        mColorGreen = ContextCompat.getColor(mContext, R.color.green);
        mColorBlue = ContextCompat.getColor(mContext, R.color.blue);
        mJRColors = JRColors;

        // Retrieve the UI objects
        mCardImageView = (ImageView) view.findViewById(R.id.cardImageView);
        mInventoryCountTextView = (RichTextView) view.findViewById(R.id.inventoryCountTextView);
    }

    @Override
    protected CardItemLoadResult doInBackground(Void... voids) {

        CardItemLoadResult result = new CardItemLoadResult();

        // 1. Process the item image
        result.ItemImage = getScaledCardImage(mSeasonID, mItem);

        // 2. Get item inventory count
        result.CountCardInventory = InventoryUtility.getInventoryCount(mSeasonID, mItem);

        // 3. Set isJR flag
        result.IsJR = mItem.Rarity.equals("JR");

        return result;
    }

    @Override
    protected void onPostExecute(CardItemLoadResult result) {
        super.onPostExecute(result);

        // 1. Display card image
        mCardImageView.setScaleType(ImageView.ScaleType.CENTER);
        mCardImageView.setImageBitmap(result.ItemImage);

        // 2. Set Card Inventory Count
        setCardInventoryCount(mInventoryCountTextView, result.CountCardInventory, result.IsJR);

        // Do some clean
        mContext = null;
        mCardImageView = null;
        mInventoryCountTextView = null;
    }

    /**
     * Set the inventory count of an item
     * @param tv
     * @param countCardInventory
     * @param isJRItem
     */
    private void setCardInventoryCount(RichTextView tv, int countCardInventory, boolean isJRItem) {

        if (!isJRItem) {
            // 1. Not a JR item

            // Configure text color
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
            StringBuilder targetStringBuilder = new StringBuilder(CardDetailActivity.JR_COLOR_TOTAL_COUNT);

            // Set displayed string based on the inventory of the item with corresponding color
            for (int i = 0; i < CardDetailActivity.JR_COLOR_TOTAL_COUNT; ++i) {
                int shiftBitCount = 3 * i;
                int currentJRItemInventory = (countCardInventory >>> shiftBitCount)
                        & CardDetailActivity.MAX_JR_INVENTORY_COUNT;
                // Check the inventory
                if (currentJRItemInventory > 0) {
                    targetStringBuilder.append("★");
                } else {
                    targetStringBuilder.append("☆");
                }
            }
            tv.setText(targetStringBuilder.toString());

            // Set displayed color based on the inventory of the item with corresponding color
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
     * Scale the image and remove the color if necessary
     * @param seasonID
     * @param cardItem
     */
    private Bitmap getScaledCardImage(SeasonID seasonID, Item cardItem) {
        // Get card image
        DatabaseFileUtility databaseFileUtility = new DatabaseFileUtility(mContext);
        Bitmap cardImage = databaseFileUtility.ReadImage(
                cardItem.ItemImage,
                DatabaseFileUtility.IMAGE_TYPE.IMAGE,
                seasonID
        );
        // Get the count of given card inventory
        int countCardInventory = InventoryUtility.getInventoryCount(seasonID, cardItem);

        // Check is cardImage NULL
        if (cardImage == null) {
            return null;
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

        return scaledBitmap;
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
