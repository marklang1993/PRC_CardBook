package com.swws.marklang.prc_cardbook.utility.inventory;

import android.app.Activity;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;
import android.util.Log;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.ItemEx;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

public class InventoryUtility {

    private static InventoryDatabase mInventoryDB = null;

    /**
     * Initialize mInventoryDB
     * @param activity The caller activity
     * @return
     */
    public static boolean initDB(Activity activity) {
        // Get access to Inventory DB
        if (mInventoryDB == null) {
            // TODO: use thread-safe way to create this singleton
            // TODO: use background thread to access DB --- remove allowMainThreadQueries()
            mInventoryDB = Room.databaseBuilder(activity.getApplicationContext(),
                    InventoryDatabase.class,
                    activity.getString(R.string.inventory_db_file_name)).allowMainThreadQueries().
                            addMigrations(new Migration(1, 2) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    /**
                     * Mirgrate the database from version 1 to version 2
                     * All rows in version 1 has the SeasonID = 0
                     */
                    database.execSQL("ALTER TABLE inventory "
                            + "ADD COLUMN SeasonID INTEGER NOT NULL DEFAULT(0)");
                }
            }).build();

            return true;
       }
       return false;
    }

    /**
     * Get the count of given card inventory
     * @param seasonID Season ID
     * @param cardItem Card Item
     * @return item count if existed, -1 if not found
     */
    public static int getInventoryCount(SeasonID seasonID, Item cardItem){
        String imageID = cardItem.getImageID();

        // Execute query
        Inventory[] cardInventory;
        if (mInventoryDB != null) {
            cardInventory = mInventoryDB.inventoryDAO().
                    queryInventoryByItemID(seasonID.ordinal(), imageID);
        } else {
            // mInventoryDB is not initialized.
            return -1;
        }

        // Get count
        int countCardInventory;
        if (cardInventory.length != 0) {
            // Item found
            countCardInventory = cardInventory[0].mInventoryItemCount;

        } else {
            // No such item is found
            countCardInventory = -1;
            Log.w("InventoryUtility",
                    String.format("DAO Query Failed with ImageID: %s, SeasonID: %s",
                            imageID,
                            seasonID.toString())
            );
        }

        return countCardInventory;
    }

    /**
     * Get the count of given card inventory
     * @param cardItem Card Item
     * @return item count if existed, -1 if not found
     */
    public static int getInventoryCount(ItemEx cardItem) {
        String imageID = cardItem.getImageID();

        // Execute query
        Inventory[] cardInventory;
        if (mInventoryDB != null) {
            cardInventory = mInventoryDB.inventoryDAO().
                    queryInventoryByItemID(cardItem.mSeasonID.ordinal(), imageID);
        } else {
            // mInventoryDB is not initialized.
            return -1;
        }

        // Get count
        int countCardInventory;
        if (cardInventory.length != 0) {
            // Item found
            countCardInventory = cardInventory[0].mInventoryItemCount;

        } else {
            // No such item is found
            countCardInventory = -1;
            Log.w("InventoryUtility",
                    String.format("DAO Query Failed with ImageID: %s, SeasonID: %s",
                            imageID,
                            cardItem.mSeasonID.toString())
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
        if (mInventoryDB != null) {
            mInventoryDB.inventoryDAO().insertInventory(newInventory);
        }
    }

    /**
     * Insert new inventory item
     * @param newInventory New inventory item
     */
    public static void insertInventoryItem(Inventory newInventory) {
        // Execute
        if (mInventoryDB != null) {
            mInventoryDB.inventoryDAO().insertInventory(newInventory);
        }
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
        if (mInventoryDB != null) {
            mInventoryDB.inventoryDAO().updateInventory(newInventory);
        }
    }

    /**
     * Query all inventory items
     * @return "null" once no result or DAO is not initialized
     */
    public static Inventory[] queryAllInventoryItems() {
        // Execute
        if (mInventoryDB != null) {
            Inventory[] queriedResult = mInventoryDB.inventoryDAO().queryAllInventories();
            if (queriedResult.length == 0) {
                return null;

            } else {
                // Return the valid result
                return queriedResult;
            }

        } else {
            return null;
        }
    }

    /**
     * Remove all inventory items
     * @return
     */
    public static boolean removeAllInventoryItems() {
        // Execute
        if (mInventoryDB != null) {
            mInventoryDB.inventoryDAO().removeAllInventories();
            return true;
        }
        return false;
    }
}
