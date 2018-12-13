package com.swws.marklang.prc_cardbook.utility.database;

import android.os.Parcel;
import android.os.Parcelable;

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
    public final static int COUNT_ELEMENT = 10;


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

    /**
     * Restore Item from a rawString
     * @param rawString
     */
    public void fromString(String rawString) {
        String tokens[] = rawString.split(",");
        // Restore Item
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
            Item restoredItem = new Item();
            restoredItem.fromString(src.readString());
            return restoredItem;
        }

        public Item[] newArray(int size)
        {
            return new Item[size];
        }
    };
}
