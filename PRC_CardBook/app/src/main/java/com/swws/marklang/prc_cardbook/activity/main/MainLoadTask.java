package com.swws.marklang.prc_cardbook.activity.main;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseFileUtility;
import com.swws.marklang.prc_cardbook.utility.MathUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseComparator;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainLoadTask extends AsyncTask<Void, Integer, Boolean> {

    private MainLoadActivity mParentActivity;
    private ProgressBar mMainLoadProgressBar;

    private DatabaseFileUtility mDatabaseFileUtility;

    // Internal database variables
    private static ArrayList<Database> mDatabases;
    private static HashMap<String, Item> mItemIDLUT;
    private static Database mLackItem2018;

    /**
     * Index: ProgressValue
     * 0    : init_item_database
     * 1    : init_item_id_lut
     * 2    : init_inventory_db
     * 3    : init_inventory_db_count
     * 4    : end
     */
    protected int[] mProgressValues = {0, 25, 55, 60, 100};

    public MainLoadTask(
            MainLoadActivity parentActivity,
            ProgressBar mainLoadProgressBar
    ) {
        super();

        // Init.
        mParentActivity = parentActivity;
        mMainLoadProgressBar = mainLoadProgressBar;

        mDatabaseFileUtility = DatabaseFileUtility.getInstance();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        // Update progress value
        publishProgress(mProgressValues[0]);


        // 1. Read meta data
        mDatabases = mDatabaseFileUtility.ReadAllMetaData(Constants.READ_ALL_METADATA_DEBUG);
        int databaseSize = mDatabases.size();
        // Update progress value
        publishProgress(mProgressValues[1]);


        // 2. Construct ItemIDLUT
        mItemIDLUT = new HashMap<>();
        for (int i = 0; i < databaseSize; ++i) {
            // Get current Database
            Database database = mDatabases.get(i);

            // Iterate all items in the current Database
            for (Item item: database) {
                String itemImageId = item.getImageID();
                mItemIDLUT.put(itemImageId, item);
            }

            // Calculate the progress and update
            int currentProgress = MathUtility.calculateCurrentProgressValue(
                    i,
                    0,
                    databaseSize,
                    mProgressValues[2],
                    mProgressValues[3] - 1);
            publishProgress(currentProgress);
        }


        // 3. Init. DB
        InventoryUtility.initDB(mParentActivity);
        // Update progress value
        publishProgress(mProgressValues[3]);


        // 4. Init. the content of Inventory DB
        for (int i = 0; i < databaseSize; ++i) {
            // Get current Database
            Database database = mDatabases.get(i);
            SeasonID dbSeasonId = database.seasonId();

            // Iterate all items in the current Database
            for (Item item: database) {
                String itemImageId = item.getImageID();

                // Check is this item registered in the inventory database
                int inventoryCount = InventoryUtility.getInventoryCount(dbSeasonId, item);
                if (inventoryCount < 0)
                {
                    // If not, insert a new record
                    InventoryUtility.insertInventoryItem(itemImageId, dbSeasonId);
                }
            }

            // Calculate the progress and update
            int currentProgress = MathUtility.calculateCurrentProgressValue(
                    i,
                    0,
                    databaseSize,
                    mProgressValues[3],
                    mProgressValues[4] - 1);
            publishProgress(currentProgress);
        }


        // 4. Sort
        // * Collections.sort() is implemented by Merge Sort, which is a stable sorting algorithm.
        Collections.sort(mDatabases, new DatabaseComparator());
        // Update progress value
        publishProgress(mProgressValues[4]);

        // 5. Query all 2018 items
        mLackItem2018 = new Database(
                MainLoadActivity.getCurrentApplicationContext().getString(R.string.database_name_lack_item_2018),
                "localhost",
                SeasonID.SEASON_1ST
        );
        for (Database database: mDatabases) {
            if ((database.seasonId() == SeasonID.SEASON_1ST) &&
                database.name().contains("弾")){
                for (Item item: database) {
                    // Check whether the user possesses this item
                    int inventoryCount = InventoryUtility.getInventoryCount(SeasonID.SEASON_1ST, item);
                    if (inventoryCount <= 0) {
                        // Does not possess
                        mLackItem2018.Insert(item);
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        // If initialization is successful, update
        // "Databases", "ItemIDLUT" and "LackItem2018" in MainActivity.
        if (aBoolean) {
            MainActivity.setAllDatabases(mDatabases);
            MainActivity.setItemIDLUT(mItemIDLUT);
            MainActivity.setLackItem2018(mLackItem2018);
        }

        // Notify the parent activity
        mParentActivity.Finished(aBoolean);

        // Do some clean
        mParentActivity = null;
        mMainLoadProgressBar = null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (values != null) {
            mMainLoadProgressBar.setProgress(values[0]);
        }
    }
}
