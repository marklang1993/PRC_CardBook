package com.swws.marklang.prc_cardbook.activity.system.inventory;

import com.swws.marklang.prc_cardbook.activity.system.SystemProgressActivity;
import com.swws.marklang.prc_cardbook.utility.MathUtility;
import com.swws.marklang.prc_cardbook.utility.inventory.Inventory;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

public class ImportInventoryDatabaseTask extends ExternalFileOperationInventoryDatabaseTaskBase {

    /**
     * Progress value
     * 0: Start
     * 1: Finish reading file
     * 2: Finish inserting all items into DB
     */
    private int[] mProgressValues = {0, 50, 100};

    public ImportInventoryDatabaseTask(SystemProgressActivity progressDisplayActivity, String fileName) {
        super(progressDisplayActivity, fileName);
    }

    // TODO: handle isCancelled
    @Override
    protected Boolean doInBackground(Void... voids) {

        // Publish the initial progress
        publishProgress(mProgressValues[0]);

        // 1. Read the inventory data from file
        InventoryFileUtility inventoryFileUtility = InventoryFileUtility.getInstance();
        Inventory[] allInventories = inventoryFileUtility.readAllInventory(
                mFileName,
                this,
                mProgressValues[0],
                mProgressValues[1]
        );

        // If reading file failed, then return false
        if (allInventories == null) return false;


        // 2. Drop the original table
        InventoryUtility.removeAllInventoryItems();

        // 3. Insert new inventory data
        int totalCount = allInventories.length;
        for (int i = 0; i < totalCount; ++i) {
            Inventory inventory = allInventories[i];
            InventoryUtility.insertInventoryItem(inventory);

            // Update progress
            int currentProgress = MathUtility.calculateCurrentProgressValue(
                    i, 0, totalCount, mProgressValues[1], mProgressValues[2]);
            postNewProgress(currentProgress);
        }

        // Finish
        postNewProgress(mProgressValues[2]);
        // Delay for 1 second
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            ;
        }

        return true;
    }
}
