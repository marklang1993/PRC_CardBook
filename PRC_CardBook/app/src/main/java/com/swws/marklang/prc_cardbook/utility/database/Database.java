package com.swws.marklang.prc_cardbook.utility.database;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.FileUtility;

import java.util.ArrayList;
import java.util.Iterator;

public class Database implements Iterable<Item>, Parcelable {

    private String _name;   // Name of this series
    private String _refUrl; // Referred URL (Key of this database)
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
            Item newItem = null;
            try {
                newItem = new Item(src.readString());

            } catch (FileUtility.InvalidDataFormatException ex) {
                Log.e(this.getClass().getSimpleName(), String.format("Invalid Data Format: %s", ex.InvalidData));
            }
            _allItems.add(newItem);
        }
    }

    /**
     * Add an item
     * @param item
     */
    public void Insert(Item item)
    {
        _allItems.add(item);
    }

    /**
     * Add an item by using an item string that contains 10 attributes
     * @param itemString
     */
    public void Insert(String itemString) throws FileUtility.InvalidDataFormatException {
        // Prepare
        Item item = new Item(itemString);
        // Insert
        Insert(item);
    }

    /**
     * Get an item of this database by its index
     * @param index
     * @return
     */
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

    /**
     * Get the corresponding URL of this database (aka. key of this database)
     * @return
     */
    public String url() {return _refUrl; }

    /**
     * Get the count of all items
     * @return
     */
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
