package com.swws.marklang.prc_cardbook.utility.inventory;

import android.util.Log;

import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.database.Item;

public class InventoryUtility {

    /**
     * Get the count of given card inventory
     * @param cardItem
     * @return
     */
    public static int getInventoryCount(Item cardItem){
        String imageID = cardItem.getImageID();
        // Execute query
        Inventory[] cardInventory = MainActivity.mInventoryDB.inventoryDAO().
                queryInventoryByItemID(imageID);
        int countCardInventory;
        if (cardInventory.length != 0) {
            // Item found
            countCardInventory = cardInventory[0].mInventoryItemCount;

        } else {
            // No such item is found - COULD BE A BUG
            countCardInventory = 0;
            Log.w("InventoryUtility", String.format("DAO Query Failed with ImageID: %s", imageID));
        }
        return countCardInventory;
    }
}
