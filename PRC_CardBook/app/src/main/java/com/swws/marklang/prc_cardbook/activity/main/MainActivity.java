package com.swws.marklang.prc_cardbook.activity.main;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.card.CardActivity;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.inventory.Inventory;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryDatabase;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static ArrayList<Database> mDatabases = null;
    public static InventoryDatabase mInventoryDB = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init. data
        initData();

        // Init. views
        setListView();
    }

    /**
     * Init. MetaData and InventoryDB
     */
    private void initData() {
        // Read meta data
        if (mDatabases == null)
        {
            FileUtility fileUtility = new FileUtility(getApplicationContext());
            mDatabases = fileUtility.ReadAllMetaData(true);
        }

        // Get access to Inventory DB
        if (mInventoryDB == null)
        {
            // TODO: use thread-safe way to create thsi singleton
            // TODO: use background thread to access DB --- remove allowMainThreadQueries()
            mInventoryDB = Room.databaseBuilder(getApplicationContext(),
                    InventoryDatabase.class,
                    getString(R.string.inventory_db_file_name)).allowMainThreadQueries().build();
        }

        // Init. the content of Inventory DB
        for (Database database: mDatabases) {
            for (Item item: database) {
                // Check is this item registered in the inventory map
                String itemImageId = item.getImageID();
                Inventory[] inventories = mInventoryDB.inventoryDAO().queryInventoryByItemID(itemImageId);
                if (inventories.length == 0)
                {
                    // If not, insert a new record
                    Inventory newInventory = new Inventory();
                    newInventory.mInventoryItemID = itemImageId;
                    newInventory.mInventoryItemCount = 0;
                    mInventoryDB.inventoryDAO().insertInventory(newInventory);
                }
            }
        }
    }

    /**
     * Set up "seriesListView"
     */
    private void setListView() {
        // Init. seriesListView
        ListView seriesListView = (ListView) findViewById(R.id.seriesListView);
        SeriesItemAdapter seriesItemAdapter = new SeriesItemAdapter(
                getApplicationContext(), mDatabases
        );
        seriesListView.setAdapter(seriesItemAdapter);
        seriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showCardActivity = new Intent(getApplicationContext(), CardActivity.class);
                // Pass params to "CardActivity"
                Database currentDatabase = mDatabases.get(position);
                showCardActivity.putExtra(CardActivity.KEY_SERIES_INDEX, position);
                showCardActivity.putExtra(CardActivity.KEY_DATABASE, currentDatabase);
                // Start
                startActivity(showCardActivity);
            }
        });
    }
}
