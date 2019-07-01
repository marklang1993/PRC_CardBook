package com.swws.marklang.prc_cardbook.activity.system.inventory;

import com.swws.marklang.prc_cardbook.activity.system.SystemProgressActivity;
import com.swws.marklang.prc_cardbook.utility.inventory.Inventory;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

public class ExportInventoryDatabaseTask extends ExternalFileOperationInventoryDatabaseTaskBase {

    /**
     * Progress value
     * 0: Start
     * 1: Finish retrieving all items from DB
     * 2: Finish writing file
     */
    private int[] mProgressValues = {0, 20, 100};

    public ExportInventoryDatabaseTask(SystemProgressActivity progressDisplayActivity, String fileName) {
        super(progressDisplayActivity, fileName);
    }

    // TODO: handle isCancelled
    @Override
    protected Boolean doInBackground(Void... voids) {

        // Publish the initial progress
        publishProgress(mProgressValues[0]);

        // Get the inventory data of all items
        Inventory[] allInventories = InventoryUtility.queryAllInventoryItems();
        publishProgress(mProgressValues[1]);

        // Write the inventory data to the file
        InventoryFileUtility inventoryFileUtility = InventoryFileUtility.getInstance();
        boolean result = inventoryFileUtility.writeAllInventory(
                mFileName,
                allInventories,
                this,
                mProgressValues[1] + 1,
                mProgressValues[2]);

        // Finish
        postNewProgress(mProgressValues[2]);
        // Delay for 1 second
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            ;
        }

        return result;
    }
}
