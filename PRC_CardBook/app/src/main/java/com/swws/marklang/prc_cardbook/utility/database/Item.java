package com.swws.marklang.prc_cardbook.utility.database;

public class Item {

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
}
