package com.swws.marklang.prc_cardbook.utility.inventory;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface InventoryDAO {

    @Query("SELECT * FROM inventory")
    Inventory[] queryAllInventories();

    @Query("SELECT * FROM inventory WHERE ItemID = :queryItemImageID AND SeasonID = :querySeasonId")
    Inventory[] queryInventoryByItemID(int querySeasonId, String queryItemImageID);

    @Insert
    void insertInventory(Inventory newInventory);

    @Update
    void updateInventory(Inventory updateInventory);

    @Query("DELETE FROM inventory")
    void removeAllInventories();

}
