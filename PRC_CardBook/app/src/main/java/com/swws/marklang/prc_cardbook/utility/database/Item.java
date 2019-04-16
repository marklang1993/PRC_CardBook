package com.swws.marklang.prc_cardbook.utility.database;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.FileUtility;

import java.io.File;

public class Item implements Parcelable {

    public String ItemImage;

    /* Item Name */
    public String InternalID;
    public String ItemName;

    /* Item Attributes */
    public String Category; // カテゴリー
    public String Type;     // タイプ
    public String Brand;    // ブランド
    public String Rarity;   // レアリティ
    public String Score;    // いいね
    public String Color;    // カラー

    public String Remarks;

    // Constants
    private final static int COUNT_ATTRIBUTES = 10;

    /**
     * Construct an empty item
     */
    public Item() {
        // Init. all attributes with null
        ItemImage = null;
        InternalID = null;
        ItemName = null;
        Category = null;
        Type = null;
        Brand = null;
        Rarity = null;
        Score = null;
        Color = null;
        Remarks = null;
    }

    /**
     * Construct this item by using an item string that contains 10 attributes
     * @param itemString
     */
    public Item(String itemString) throws FileUtility.InvalidDataFormatException {

        String[] tokens = itemString.split(",");
        if (tokens.length != Item.COUNT_ATTRIBUTES) {
            throw new FileUtility.InvalidDataFormatException(itemString);
        }

        // Init. all attributes
        ItemImage = tokens[0];
        InternalID = tokens[1];
        ItemName = tokens[2];
        Category = tokens[3];
        Type = tokens[4];
        Brand = tokens[5];
        Rarity = tokens[6];
        Score = tokens[7];
        Color = tokens[8];
        Remarks = tokens[9];
    }

    /**
     * Get the id of the item image
     * @return
     */
    public String getImageID() {
        String imageFileName = (new File(ItemImage)).getName();
        String imageID = imageFileName.substring(
                0, imageFileName.lastIndexOf('.'));
        return imageID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(ItemImage);
        sb.append(",");
        sb.append(InternalID);
        sb.append(",");
        sb.append(ItemName);
        sb.append(",");
        sb.append(Category);
        sb.append(",");
        sb.append(Type);
        sb.append(",");
        sb.append(Brand);
        sb.append(",");
        sb.append(Rarity);
        sb.append(",");
        sb.append(Score);
        sb.append(",");
        sb.append(Color);
        sb.append(",");
        sb.append(Remarks);

        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toString());
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>()
    {
        public Item createFromParcel(Parcel src)
        {
            Item restoredItem = null;
            try {
                restoredItem = new Item(src.readString());
            } catch (FileUtility.InvalidDataFormatException ex) {
                Log.e(this.getClass().getSimpleName(), String.format("Invalid Data Format: %s", ex.InvalidData));
            }

            return restoredItem;
        }

        public Item[] newArray(int size)
        {
            return new Item[size];
        }
    };
}
