package com.swws.marklang.prc_cardbook.utility.database;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;

public class Database implements Iterable<Item>, Parcelable {

    private String _name;   // Name of this series
    private String _refUrl; // Referred URL
    private ArrayList<Item> _allItems; // All items

    public Database(String name, String refUrl)
    {
        // Init.
        _name = name;
        _refUrl = refUrl;

        _allItems = new ArrayList<>();
    }

    public Database(Parcel src)
    {
        // Read basic info.
        _name = src.readString();
        _refUrl = src.readString();

        // Read item info.
        int databaseSize = src.readInt();
        _allItems = new ArrayList<>();
        for (int i = 0; i < databaseSize; ++i)
        {
            Item newItem = new Item();
            newItem.fromString(src.readString());
            _allItems.add(newItem);
        }
    }

    public void Insert(Item item)
    {
        _allItems.add(item);
    }

    public void Insert(
            String image,
            String id,
            String name,
            String category,
            String type,
            String brand,
            String rarity,
            String score,
            String color,
            String remarks
    )
    {
        // Prepare
        Item item = new Item();

        item.ItemImage = image;

        item.InternalID = id;
        item.ItemName = name;

        item.Category = category;
        item.Type = type;
        item.Brand = brand;
        item.Rarity = rarity;
        item.Score = score;
        item.Color = color;

        item.Remarks = remarks;

        // Insert
        Insert(item);
    }

    public Item get(int index)
    {
        if (index >=0 && index < _allItems.size())
        {
            return _allItems.get(index);
        }
        return null;
    }

    public String name()
    {
        return _name;
    }

    public String url() {return _refUrl; }

    public int size()
    {
        return _allItems.size();
    }

    @Override
    public boolean equals(Object obj) {
        Database otherDatabase = (Database) obj;

        // Compare based on the _refUrl
        return otherDatabase._refUrl.equals(this._refUrl);
    }

    @Override
    public Iterator<Item> iterator() {
        return _allItems.iterator();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write basic info.
        dest.writeString(_name);
        dest.writeString(_refUrl);

        // Write item info.
        dest.writeInt(_allItems.size());
        for (Item item: _allItems) {
            dest.writeString(item.toString());
        }
    }

    public static final Parcelable.Creator<Database> CREATOR = new Parcelable.Creator<Database>()
    {
        public Database createFromParcel(Parcel src)
        {
            return new Database(src);
        }

        public Database[] newArray(int size)
        {
            return new Database[size];
        }
    };
}
