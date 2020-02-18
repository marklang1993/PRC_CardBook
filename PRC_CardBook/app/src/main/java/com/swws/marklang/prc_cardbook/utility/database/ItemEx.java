package com.swws.marklang.prc_cardbook.utility.database;


/**
 * Item with "SeasonID" field
 */
public class ItemEx extends Item {

    public SeasonID mSeasonID;
    public String mSeriesName;

    public ItemEx() {
        mSeasonID = null;
        mSeriesName = null;
    }

    /**
     * Construct an ItemEx object from Item object
     * @param item
     * @param seasonID
     * @param seriesName
     */
    public ItemEx(Item item, SeasonID seasonID, String seriesName){
        mSeasonID = seasonID;
        mSeriesName = seriesName;

        ItemImage = item.ItemImage;
        InternalID = item.InternalID;
        ItemName = item.ItemName;
        Category = item.Category;
        Type = item.Type;
        Brand = item.Brand;
        Rarity = item.Rarity;
        Score = item.Score;
        Color = item.Color;
        Remarks = item.Remarks;
    }
}

