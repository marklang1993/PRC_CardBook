package com.swws.marklang.prc_cardbook.activity.card;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseFileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

public class CardDetailActivity extends AppCompatActivity {

    public static final String KEY_ITEM_INDEX = "com.swws.marklang.prc_cardbook.ITEM_INDEX";

    private static final float CARD_BIG_IMAGE_SIZE_SP = 180.0f; // TODO: size

    // Boundary of inventory count
    public static final int MIN_INVENTORY_COUNT = 0;
    public static final int MAX_INVENTORY_COUNT = 0xFFFF;
    public static final int MAX_JR_INVENTORY_COUNT = 0x7;

    // Internal variables
    private int mCardItemIndex = 0;
    private Item mCardItem = null;
    private SeasonID mSeasonID = null;
    private int mInventoryCount = 0; // the current count of this card in inventory
    private JRColor mJRColor = JRColor.UNKNOWN;

    private ClipboardManager mClipboardManager;
    private ClipData mClipData;

    // Inventory JR color selection buttons
    private Button mInventoryJRColorPinkButton;
    private Button mInventoryJRColorYellowButton;
    private Button mInventoryJRColorBlueButton;
    private Button mInventoryJRColorRedButton;
    private Button mInventoryJRColorGreenButton;
    private Button mInventoryJRColorPurpleButton;
    private Button mInventoryJRColorBlackButton;
    private Button mInventoryJRColorGoldButton;


    // JR color enum
    private enum JRColor {
        PINK, YELLOW, BLUE, RED, GREEN, PURPLE, BLACK, GOLD,
        UNKNOWN, NOT_JR
    }

    // JR color string look-up table
    private static final String[] JR_STRING_LUT = {
            "ピンク", "イエロー", "ブルー", "レッド", "グリーン", "パープル", "ブラック", "ゴールド",
            "UNKNOWN", "NOT_JR"
    };

    // Item color string look-up table
    private static final String[] ITEM_STRING_LUT = {
            "ピンク", "黄", "青", "赤", "緑", "紫", "黒", "ゴールド",
            "UNKNOWN", "NOT_JR"
    };
    public static final int JR_COLOR_TOTAL_COUNT = 8;


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

        // Get season id and card item
        mSeasonID = CardActivity.getSeasonID();
        mCardItem = CardActivity.getItemByIndex(mCardItemIndex);
        if (!mCardItem.Rarity.equals("JR")) {
            mJRColor = JRColor.NOT_JR;
        }

        // Configure the JR color if this is an item with rarity of JR
        if (mJRColor != JRColor.NOT_JR) {
            configureJRColor();
        }

        // Get the current count of this card in inventory
        mInventoryCount = InventoryUtility.getInventoryCount(mSeasonID, mCardItem);
        if (mInventoryCount < 0) {
            Log.e(this.getClass().getName(), "This item does not have an Inventory record in DB.");
            return;
        }

        // Init. mClipboardManager & mClipData
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipData = ClipData.newPlainText("PRC_CardBook Text", mCardItem.ItemName);

        // Init. UIs
        setTitle(getString(R.string.card_detail_activity_name));
        uiLoadCard();
        initButtons();
    }

    /**
     * Configure the JR color
     */
    private void configureJRColor() {
        if (mJRColor != JRColor.NOT_JR) {
            // Determine the color
            JRColor[] arrayJRColor = JRColor.values();

            for (int i = 0; i < JR_COLOR_TOTAL_COUNT; ++i) {
                if (mCardItem.Color.contains(ITEM_STRING_LUT[i])) {
                    mJRColor = arrayJRColor[i];
                    return;
                }
            }

            // Does not recognize
            mJRColor = JRColor.UNKNOWN;
        }
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * UI loads card details
     * @return
     */
    private void uiLoadCard() {
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
        setImageByScaling(cardBigImageView, mCardItem.ItemImage, DatabaseFileUtility.IMAGE_TYPE.IMAGE, CARD_BIG_IMAGE_SIZE_SP);
        setImageByScaling(brandContentTextView, mCardItem.Brand, DatabaseFileUtility.IMAGE_TYPE.BRAND,-1.0f);
        setImageByScaling(typeContentImageView, mCardItem.Type, DatabaseFileUtility.IMAGE_TYPE.TYPE,-1.0f);

        // Set values for static information
        cardNameTextView.setText(mCardItem.ItemName.replace(' ', '\n'));
        categoryContentTextView.setText(mCardItem.Category);
        colorContentTextView.setText(mCardItem.Color);
        rarityContentTextView.setText(mCardItem.Rarity);
        scoreContentTextView.setText(mCardItem.Score);

        // Add ClickListener to copy the item name to ClipBoard
        cardNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClipboardManager.setPrimaryClip(mClipData);

                Toast.makeText(
                        CardDetailActivity.this,
                        getString(R.string.card_detail_item_name_copied),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // Display inventory value
        updateInventoryValueDisplay();
    }

    /**
     * Init. buttons
     */
    private void initButtons() {
        // Increase Inventory Button
        Button inventoryIncreaseButton = (Button) findViewById(R.id.inventoryIncreaseButton);
        inventoryIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increase inventory
                int count = getCurrentItemInventoryCount();
                ++count;
                updateInventoryCount(count);
            }
        });

        // Decrease Inventory Button
        Button inventoryDecreaseButton = (Button) findViewById(R.id.inventoryDecreaseButton);
        inventoryDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Decrease inventory
                int count = getCurrentItemInventoryCount();
                --count;
                updateInventoryCount(count);
            }
        });

        // Back Button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable

        // Configure TableRow for JR Color Selection Buttons
        if (mJRColor == JRColor.NOT_JR) {
            // NOT a JR item
            TableLayout inventoryJRColorSelectionTableLayout = findViewById(R.id.inventoryJRColorSelectionTableLayout);
            inventoryJRColorSelectionTableLayout.setVisibility(View.INVISIBLE);

        } else {
            // JR item
            mInventoryJRColorPinkButton = (Button) findViewById(R.id.inventoryJRColorPinkButton);
            mInventoryJRColorYellowButton = (Button) findViewById(R.id.inventoryJRColorYellowButton);
            mInventoryJRColorBlueButton = (Button) findViewById(R.id.inventoryJRColorBlueButton);
            mInventoryJRColorRedButton = (Button) findViewById(R.id.inventoryJRColorRedButton);
            mInventoryJRColorGreenButton = (Button) findViewById(R.id.inventoryJRColorGreenButton);
            mInventoryJRColorPurpleButton = (Button) findViewById(R.id.inventoryJRColorPurpleButton);
            mInventoryJRColorBlackButton = (Button) findViewById(R.id.inventoryJRColorBlackButton);
            mInventoryJRColorGoldButton = (Button) findViewById(R.id.inventoryJRColorGoldButton);

            mInventoryJRColorPinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.PINK);
                }
            });

            mInventoryJRColorYellowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.YELLOW);
                }
            });

            mInventoryJRColorBlueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.BLUE);
                }
            });

            mInventoryJRColorRedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.RED);
                }
            });

            mInventoryJRColorGreenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.GREEN);
                }
            });

            mInventoryJRColorPurpleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.PURPLE);
                }
            });

            mInventoryJRColorBlackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.BLACK);
                }
            });

            mInventoryJRColorGoldButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUIAfterSelectingJRColor(JRColor.GOLD);
                }
            });

            // Update JR color selection button states
            updateJRColorSelectButtons();
        }

    }

    /**
     * Update corresponding UI after selecting new JR color
     * @param newColor
     */
    private void updateUIAfterSelectingJRColor(JRColor newColor) {
        // Find corresponding UI components
        ImageView cardBigImageView = (ImageView) findViewById(R.id.cardBigImageView); // TODO
        TextView cardNameTextView = (TextView) findViewById(R.id.cardNameTextView);
        TextView colorContentTextView = (TextView) findViewById(R.id.colorContentTextView);

        // Save the old color string in item name
        String oldJRColorString = JR_STRING_LUT[mJRColor.ordinal()];

        // Set new color
        mJRColor = newColor;

        // Set the new color string in item name
        String newJRColorString = JR_STRING_LUT[mJRColor.ordinal()];
        String newCardName = cardNameTextView.getText().toString();
        newCardName = newCardName.replaceAll(oldJRColorString, newJRColorString);
        cardNameTextView.setText(newCardName);

        // Set the new color content
        String newItemColorString = ITEM_STRING_LUT[mJRColor.ordinal()];
        colorContentTextView.setText(newItemColorString);

        // Update other UI components
        updateJRColorSelectButtons();
        updateInventoryValueDisplay();
    }

    /**
     * Update JR color selection button states
     */
    private void updateJRColorSelectButtons() {
        mInventoryJRColorPinkButton.setVisibility(mJRColor == JRColor.PINK ? View.INVISIBLE : View.VISIBLE);
        mInventoryJRColorYellowButton.setVisibility(mJRColor == JRColor.YELLOW ? View.INVISIBLE : View.VISIBLE);
        mInventoryJRColorBlueButton.setVisibility(mJRColor == JRColor.BLUE ? View.INVISIBLE : View.VISIBLE);
        mInventoryJRColorRedButton.setVisibility(mJRColor == JRColor.RED ? View.INVISIBLE : View.VISIBLE);
        mInventoryJRColorGreenButton.setVisibility(mJRColor == JRColor.GREEN ? View.INVISIBLE : View.VISIBLE);
        mInventoryJRColorPurpleButton.setVisibility(mJRColor == JRColor.PURPLE ? View.INVISIBLE : View.VISIBLE);
        mInventoryJRColorBlackButton.setVisibility(mJRColor == JRColor.BLACK ? View.INVISIBLE : View.VISIBLE);
        mInventoryJRColorGoldButton.setVisibility(mJRColor == JRColor.GOLD ? View.INVISIBLE : View.VISIBLE);
    }

    /**
     * Update the inventory count in DB and display
     * Also, notify "CardActivity" to update UI
     * @param count
     */
    private void updateInventoryCount(int count) {
        if (setCurrentItemInventoryCount(count)) {
            // Update inventory count in the Database
            InventoryUtility.updateInventoryItem(
                    mCardItem.getImageID(),
                    mInventoryCount,
                    mSeasonID);
            // Update inventory value display
            updateInventoryValueDisplay();
            // Set result to notify "CardActivity"
            setResult(RESULT_OK);
        }
    }

    /**
     * Get current item inventory count
     * @return
     */
    private int getCurrentItemInventoryCount() {
        if (mJRColor == JRColor.NOT_JR) {
            // Not JR item
            return mInventoryCount;

        } else {
            // JR item
            int shiftBitCount = 3 * mJRColor.ordinal();
            int rawCount = mInventoryCount >>> shiftBitCount;
            return rawCount & MAX_JR_INVENTORY_COUNT;
        }
    }

    /**
     * Set current item inventory count
     * @param count
     * @return
     */
    private boolean setCurrentItemInventoryCount(int count) {
        if (count < MIN_INVENTORY_COUNT) return false;

        if (mJRColor == JRColor.NOT_JR) {
            // Not JR item
            if (count <= MAX_INVENTORY_COUNT) {
                mInventoryCount = count;
            }

        } else {
            // JR item
            if (count <= MAX_JR_INVENTORY_COUNT) {
                int shiftBitCount = 3 * mJRColor.ordinal();
                int rawCount = ((count & MAX_JR_INVENTORY_COUNT) << shiftBitCount);

                // Clear the current area
                int clearMask = ~(MAX_JR_INVENTORY_COUNT << shiftBitCount);
                mInventoryCount = mInventoryCount & clearMask;

                // Add the rawCount
                mInventoryCount = rawCount | mInventoryCount;
            }
        }
        return true;
    }

    /**
     * Update inventory value display
     */
    private void updateInventoryValueDisplay() {
        TextView inventoryCountContentTextView = (TextView) findViewById(R.id.inventoryCountContentTextView);

        int currentInventoryCount = getCurrentItemInventoryCount();
        inventoryCountContentTextView.setText(String.valueOf(currentInventoryCount));
    }

    /**
     * Scale the image and load it to ImageView iv
     * @param iv
     * @param imageOnlinePath
     * @param targetSize
     */
    private void setImageByScaling(ImageView iv, String imageOnlinePath, DatabaseFileUtility.IMAGE_TYPE imageType, float targetSize) {
        // Get image
        DatabaseFileUtility databaseFileUtility = new DatabaseFileUtility(getApplicationContext());
        Bitmap image = databaseFileUtility.ReadImage(imageOnlinePath, imageType, mSeasonID);

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
