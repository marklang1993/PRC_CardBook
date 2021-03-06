package com.swws.marklang.prc_cardbook.activity.card;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.activity.setting.SettingFileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

public class CardActivity extends AppCompatActivity {

    public static final String KEY_SERIES_INDEX = "com.swws.marklang.prc_cardbook.SERIES_INDEX";

    private static Database mDatabase = null;
    private boolean mIsLongClickManner = false;

    private CardItemAdapter mCardItemAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // Extract passed in info.
        Intent intent = getIntent();
        int seriesIndex;
        if (intent.hasExtra(KEY_SERIES_INDEX)) {
            seriesIndex = intent.getExtras().getInt(KEY_SERIES_INDEX);

        } else {
            Log.e(this.getClass().getName(), KEY_SERIES_INDEX + " NOT FOUND!");
            mDatabase = null;
            finish();
            return;
        }

        // Get corresponding database
        if (seriesIndex < 0) {
            mDatabase = MainActivity.getLackItem2018();

        } else {
            mDatabase = MainActivity.getDatabaseByIndex(seriesIndex);
        }

        // Get operation manner option
        SettingFileUtility settingFileUtility = SettingFileUtility.getInstance();
        mIsLongClickManner = settingFileUtility.getBooleanValue(
                settingFileUtility.readItem("display_detail_page_by_long_click"));

        // Initialize UI components
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stopping all pending loading tasks
        mCardItemAdapter.stopLoading();
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
     * Notify "cardGridView" to update once the CardDetailActivity finished.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_AR_CARD_INVENTORY_CHANGE) {
            if (resultCode == RESULT_OK) {
                // Update "cardGridView" if the inventory of any item changed
                mCardItemAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Init. all UI components
     */
    private void initUI() {
        // Set title
        setTitle(mDatabase.name());

        // Set cardGridView
        GridView cardGridView = (GridView) findViewById(R.id.cardGridView);
        mCardItemAdapter = new CardItemAdapter(getApplicationContext(), mDatabase, getResources());
        cardGridView.setAdapter(mCardItemAdapter);

        // Set Click Listener
        if (mIsLongClickManner) {
            // 1. Long Click Manner (ONLY valid for non-JR items)

            // Set onItemClickListener
            cardGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SeasonID seasonID = mDatabase.seasonId();
                    int cardItemIndex = position;
                    Item cardItem = CardActivity.getItemByIndex(cardItemIndex);

                    if (!cardItem.Rarity.equals("JR")) {
                        // (1). Non-JR item

                        // Get the current count of this card in inventory
                        int inventoryCount = InventoryUtility.getInventoryCount(seasonID, cardItem);
                        // Increase inventory
                        ++inventoryCount;
                        // Update inventory count in the Database
                        InventoryUtility.updateInventoryItem(
                                cardItem.getImageID(),
                                inventoryCount,
                                seasonID
                        );

                        // Update displayed inventory value
                        mCardItemAdapter.notifyDataSetChanged();

                    } else {
                        // (2). JR item
                        Intent showCardDetailActivity = new Intent(getApplicationContext(), CardDetailActivity.class);

                        // Passing params
                        showCardDetailActivity.putExtra(CardDetailActivity.KEY_CARD_DETAIL_START_TYPE, CardDetailActivity.StartType.CARD.toString());
                        showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_INDEX, position);
                        showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_SEASON_ID, mDatabase.seasonId().toString());

                        // Start the CardDetailActivity
                        startActivityForResult(showCardDetailActivity, Constants.REQUEST_AR_CARD_INVENTORY_CHANGE);
                    }
                }
            });

            // Set onItemLongClickListener
            cardGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent showCardDetailActivity = new Intent(getApplicationContext(), CardDetailActivity.class);

                    // Passing params
                    showCardDetailActivity.putExtra(CardDetailActivity.KEY_CARD_DETAIL_START_TYPE, CardDetailActivity.StartType.CARD.toString());
                    showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_INDEX, position);
                    showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_SEASON_ID, mDatabase.seasonId().toString());

                    // Start the CardDetailActivity
                    startActivityForResult(showCardDetailActivity, Constants.REQUEST_AR_CARD_INVENTORY_CHANGE);

                    return true;
                }
            });

        } else {
            // 2. Normal Click Manner

            // Set onItemClickListener
            cardGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent showCardDetailActivity = new Intent(getApplicationContext(), CardDetailActivity.class);

                    // Passing params
                    showCardDetailActivity.putExtra(CardDetailActivity.KEY_CARD_DETAIL_START_TYPE, CardDetailActivity.StartType.CARD.toString());
                    showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_INDEX, position);
                    showCardDetailActivity.putExtra(CardDetailActivity.KEY_ITEM_SEASON_ID, mDatabase.seasonId().toString());

                    // Start the CardDetailActivity
                    startActivityForResult(showCardDetailActivity, Constants.REQUEST_AR_CARD_INVENTORY_CHANGE);
                }
            });
        }

        // Display Back Button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Get item by index
     * @param itemIndex Item index
     * @return Item with specified index
     */
    public static Item getItemByIndex(int itemIndex) {
        return mDatabase.get(itemIndex);
    }
}
