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

    /**
     * SeasonID
     * 0: 1st Season
     * 1: 2nd Season
     */
    @ColumnInfo(name = "SeasonID")
    public int mSeasonID;
}
