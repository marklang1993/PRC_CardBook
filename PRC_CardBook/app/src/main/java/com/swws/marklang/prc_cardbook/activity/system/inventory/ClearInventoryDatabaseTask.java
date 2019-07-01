package com.swws.marklang.prc_cardbook.activity.system.inventory;

import com.swws.marklang.prc_cardbook.activity.system.SystemProgressActivity;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;


public class ClearInventoryDatabaseTask extends InventoryDatabaseOperationTaskBase {

    /**
     * Progress value
     * 0: Start
     * 1: Finish clean the inventory DB
     */
    private int[] mProgressValues = {0, 100};

    public ClearInventoryDatabaseTask(SystemProgressActivity progressDisplayActivity) {
        super(progressDisplayActivity);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        // Publish the initial progress
        publishProgress(mProgressValues[0]);

        // 1. Drop the entire table
        InventoryUtility.removeAllInventoryItems();

        // Finish
        postNewProgress(mProgressValues[1]);
        // Delay for 1 second
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            ;
        }

        return true;
    }
}
