package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Item;

public class CardDetailActivity extends AppCompatActivity {

    public static final String KEY_ITEM = "com.swws.marklang.prc_cardbook.ITEM";

    private static final float CARD_BIG_IMAGE_SIZE_SP = 180.0f; // TODO: size

    private Item mCardItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);

        // Get passed in CardItem
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_ITEM)) {
            mCardItem = (Item) intent.getExtras().getParcelable(KEY_ITEM);
        } else {
            Log.e(this.getClass().getName(), KEY_ITEM + " NOT FOUND!");
            return;
        }

        // Load Card Details
        loadCard();
    }

    private void loadCard() {
        ImageView cardBigImageView = (ImageView) findViewById(R.id.cardBigImageView);
        ImageView brandContentTextView = (ImageView) findViewById(R.id.brandContentImageView);
        ImageView typeContentImageView = (ImageView) findViewById(R.id.typeContentImageView);
        TextView cardNameTextView = (TextView) findViewById(R.id.cardNameTextView);
        TextView categoryContentTextView = (TextView) findViewById(R.id.categoryContentTextView);
        TextView colorContentTextView = (TextView) findViewById(R.id.colorContentTextView);
        TextView rarityContentTextView = (TextView) findViewById(R.id.rarityContentTextView);
        TextView scoreContentTextView = (TextView) findViewById(R.id.scoreContentTextView);

        // Set images
        setImageByScaling(cardBigImageView, mCardItem.ItemImage, FileUtility.IMAGE_TYPE.IMAGE, CARD_BIG_IMAGE_SIZE_SP);
        setImageByScaling(brandContentTextView, mCardItem.Brand, FileUtility.IMAGE_TYPE.BRAND,-1.0f);
        setImageByScaling(typeContentImageView, mCardItem.Type, FileUtility.IMAGE_TYPE.TYPE,-1.0f);

        // Set values
        cardNameTextView.setText(mCardItem.ItemName.replace(' ', '\n'));
        categoryContentTextView.setText(mCardItem.Category);
        colorContentTextView.setText(mCardItem.Color);
        rarityContentTextView.setText(mCardItem.Rarity);
        scoreContentTextView.setText(mCardItem.Score);
    }

    /**
     * Scale the image and load it to ImageView iv
     * @param iv
     * @param imageOnlinePath
     * @param targetSize
     */
    private void setImageByScaling(ImageView iv, String imageOnlinePath, FileUtility.IMAGE_TYPE imageType, float targetSize) {
        // Get image
        FileUtility fileUtility = new FileUtility(getApplicationContext());
        Bitmap image = fileUtility.ReadImage(imageOnlinePath, imageType);

        // Check is image NULL
        if (image == null)
        {
            return;
        }

        // Scale and set image
        iv.setScaleType(ImageView.ScaleType.CENTER);
        if (targetSize > 0.0f) {
            // NEED to Scale
            float side = targetSize * getResources().getDisplayMetrics().scaledDensity;
            iv.setImageBitmap(
                    Bitmap.createScaledBitmap(
                            image,
                            (int)side,
                            (int)side,
                            false
                    )
            );
        } else {
            // NO need to Scale
            iv.setImageBitmap(image);
        }
    }

}
