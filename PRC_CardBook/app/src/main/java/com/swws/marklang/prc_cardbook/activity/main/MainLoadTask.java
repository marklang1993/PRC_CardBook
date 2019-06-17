package com.swws.marklang.prc_cardbook.activity.main;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.MathUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseComparator;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

import java.util.ArrayList;
import java.util.Collections;

public class MainLoadTask extends AsyncTask<Void, Integer, Boolean> {

    private MainLoadActivity mParentActivity;
    private ProgressBar mMainLoadProgressBar;

    private FileUtility mFileUtility;

    private static ArrayList<Database> mDatabases;

    /**
     * Index: ProgressValue
     * 0    : init_item_database
     * 1    : init_inventory_db
     * 2    : init_inventory_db_count
     * 3    : end
     */
    protected int[] mProgressValues = {0, 55, 60, 100};

    public MainLoadTask(
            MainLoadActivity parentActivity,
            ProgressBar mainLoadProgressBar
    ) {
        super();

        // Init.
        mParentActivity = parentActivity;
        mMainLoadProgressBar = mainLoadProgressBar;

        mFileUtility = new FileUtility(parentActivity.getApplicationContext());
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        // Update progress value
        publishProgress(mProgressValues[0]);

        // 1. Read meta data
        mDatabases = mFileUtility.ReadAllMetaData(Constants.READ_ALL_METADATA_DEBUG);
        // Update progress value
        publishProgress(mProgressValues[1]);

        // 2. Init. DB
        InventoryUtility.initDB(mParentActivity);
        // Update progress value
        publishProgress(mProgressValues[2]);

        // 3. Init. the content of Inventory DB
        for (int i = 0; i < mDatabases.size(); ++i) {
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
                    mDatabases.size(),
                    mProgressValues[2],
                    mProgressValues[3] - 1);
            publishProgress(currentProgress);
        }

        // Sort
        // * Collections.sort() implemented by Merge Sort which is stable.
        Collections.sort(mDatabases, new DatabaseComparator());

        // Update progress value
        publishProgress(mProgressValues[3]);

        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        // If initialization is successful, update Databases in MainActivity.
        if (aBoolean) {
            MainActivity.setAllDatabases(mDatabases);
        }

        // Notify the parent activity
        mParentActivity.Finished(aBoolean);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (values != null) {
            mMainLoadProgressBar.setProgress(values[0]);
        }
    }
}
