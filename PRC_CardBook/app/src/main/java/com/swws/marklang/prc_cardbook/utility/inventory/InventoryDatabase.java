package com.swws.marklang.prc_cardbook.utility.inventory;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Inventory.class}, version = 1)
public abstract class InventoryDatabase extends RoomDatabase {
    public abstract InventoryDAO inventoryDAO();
}