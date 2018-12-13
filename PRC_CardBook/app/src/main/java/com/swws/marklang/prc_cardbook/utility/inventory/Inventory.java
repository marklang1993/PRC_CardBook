package com.swws.marklang.prc_cardbook.utility.inventory;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "inventory")
public class Inventory {

    @PrimaryKey
    @ColumnInfo(name = "ItemID")
    @NonNull
    public String mInventoryItemID;

    @ColumnInfo(name = "ItemCount")
    public int mInventoryItemCount;
}
