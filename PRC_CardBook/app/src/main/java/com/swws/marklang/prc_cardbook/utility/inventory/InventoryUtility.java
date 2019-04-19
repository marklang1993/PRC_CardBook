package com.swws.marklang.prc_cardbook.utility.inventory;

import android.util.Log;

import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

public class InventoryUtility {

    /**
     * Get the count of given card inventory
     * @param seasonID Season ID
     * @param cardItem Card Item
     * @return item count if existed, -1 if not found
     */
    public static int getInventoryCount(SeasonID seasonID, Item cardItem){
        String imageID = cardItem.getImageID();
        // Execute query
        Inventory[] cardInventory = MainActivity.mInventoryDB.inventoryDAO().
                queryInventoryByItemID(seasonID.ordinal(), imageID);

        int countCardInventory;
        if (cardInventory.length != 0) {
            // Item found
            countCardInventory = cardInventory[0].mInventoryItemCount;

        } else {
            // No such item is found
            countCardInventory = -1;
            Log.w("InventoryUtility",
                    String.format("DAO Query Failed with ImageID: %s, SeasonID: %d",
                            imageID,
                            seasonID)
            );
        }
        return countCardInventory;
    }


    /**
     * Insert new inventory item
     * @param itemID Item ID (Key)
     * @param seasonID Season ID
     */
    public static void insertInventoryItem(String itemID, SeasonID seasonID) {
        // Init. the target inventory object
        Inventory newInventory = new Inventory();
        newInventory.mInventoryItemID = itemID;
        newInventory.mInventoryItemCount = 0;  // By default, the count is 0
        newInventory.mSeasonID = seasonID.ordinal();
        // Execute
        MainActivity.mInventoryDB.inventoryDAO().insertInventory(newInventory);
    }

    /**
     * Update an existed inventory item
     * @param itemID Item ID (Key)
     * @param itemCount New count of this item
     * @param seasonID Season ID
     */
    public static void updateInventoryItem(String itemID, int itemCount, SeasonID seasonID) {
        // Init. the target inventory object
        Inventory newInventory = new Inventory();
        newInventory.mInventoryItemID = itemID;
        newInventory.mInventoryItemCount = itemCount;
        newInventory.mSeasonID = seasonID.ordinal();
        // Execute
        MainActivity.mInventoryDB.inventoryDAO().updateInventory(newInventory);
    }
}
