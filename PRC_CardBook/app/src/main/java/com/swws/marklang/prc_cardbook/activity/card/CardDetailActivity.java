package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.inventory.Inventory;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

public class CardDetailActivity extends AppCompatActivity {

    public static final String KEY_ITEM_INDEX = "com.swws.marklang.prc_cardbook.ITEM_INDEX";

    private static final float CARD_BIG_IMAGE_SIZE_SP = 180.0f; // TODO: size

    private int mCardItemIndex = 0;
    private Item mCardItem = null;
    private int mInventoryCount = 0; // the current count of this card in inventory

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);

        // Get param passed in
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_ITEM_INDEX)) {
            mCardItemIndex = intent.getExtras().getInt(KEY_ITEM_INDEX);
        } else {
            Log.e(this.getClass().getName(), KEY_ITEM_INDEX + " NOT FOUND!");
            return;
        }
        mCardItem = CardActivity.getItemByIndex(mCardItemIndex);

        // Get the current count of this card in inventory
        mInventoryCount = InventoryUtility.getInventoryCount(mCardItem);

        // Load Card Details
        loadCard();

        // Init. buttons
        initButtons();
    }

    /**
     * Load card details
     * @return
     */
    private void loadCard() {
        // Card static information
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

        // Set values for static information
        cardNameTextView.setText(mCardItem.ItemName.replace(' ', '\n'));
        categoryContentTextView.setText(mCardItem.Category);
        colorContentTextView.setText(mCardItem.Color);
        rarityContentTextView.setText(mCardItem.Rarity);
        scoreContentTextView.setText(mCardItem.Score);

        // Set inventory value
        updateInventoryValue();
    }

    /**
     * Init. buttons
     */
    private void initButtons() {
        Button inventoryIncreaseButton = (Button) findViewById(R.id.inventoryIncreaseButton);
        inventoryIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increase inventory
                ++mInventoryCount;
                // Update database
                Inventory newInventory = new Inventory();
                newInventory.mInventoryItemID = mCardItem.getImageID();
                newInventory.mInventoryItemCount = mInventoryCount;
                MainActivity.mInventoryDB.inventoryDAO().updateInventory(newInventory);
                // Update inventory value
                updateInventoryValue();
                // Set result to notify "CardActivity"
                setResult(0);
            }
        });

        Button inventoryDecreaseButton = (Button) findViewById(R.id.inventoryDecreaseButton);
        inventoryDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInventoryCount > 0) {
                    // Decrease inventory
                    --mInventoryCount;
                    // Update database
                    Inventory newInventory = new Inventory();
                    newInventory.mInventoryItemID = mCardItem.getImageID();
                    newInventory.mInventoryItemCount = mInventoryCount;
                    MainActivity.mInventoryDB.inventoryDAO().updateInventory(newInventory);
                    // Update inventory value
                    updateInventoryValue();
                    // Set result to notify "CardActivity"
                    setResult(0);
                }
            }
        });
    }

    /**
     * Update inventory value display
     */
    private void updateInventoryValue() {
        TextView inventoryCountContentTextView = (TextView) findViewById(R.id.inventoryCountContentTextView);
        inventoryCountContentTextView.setText(mInventoryCount + "");
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
