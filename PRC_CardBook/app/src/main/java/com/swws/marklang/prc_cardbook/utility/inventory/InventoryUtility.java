package com.swws.marklang.prc_cardbook.utility.inventory;

import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.database.Item;

public class InventoryUtility {

    /**
     * // Get the count of given card inventory
     * @param cardItem
     * @return
     */
    public static int getInventoryCount(Item cardItem){
        Inventory[] cardInventory = MainActivity.mInventoryDB.inventoryDAO().
                queryInventoryByItemID(cardItem.getImageID());
        int countCardInventory;
        if (cardInventory.length != 0) {
            countCardInventory = cardInventory[0].mInventoryItemCount;
        } else {
            countCardInventory = 0;
        }
        return countCardInventory;
    }
}
