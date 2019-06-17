package com.swws.marklang.prc_cardbook.utility.database;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.FileUtility;

import java.util.ArrayList;
import java.util.Iterator;

public class Database implements Iterable<Item>, Parcelable {

    private String mName;       // Name of this series
    private String mRefUrl;     // Referred URL (Key of this database)
    private SeasonID mSeasonID; // Season ID

    private ArrayList<Item> mAllItems; // All items

    /**
     * Constructor
     * @param name Name of this series
     * @param refUrl Referred URL (Key of this database)
     * @param seasonID Season ID
     */
    public Database(String name, String refUrl, SeasonID seasonID)
    {
        // Init.
        mName = name;
        mRefUrl = refUrl;
        mSeasonID = seasonID;

        mAllItems = new ArrayList<>();
    }

    public Database(Parcel src)
    {
        // Read basic info.
        mName = src.readString();
        mRefUrl = src.readString();
        mSeasonID = SeasonID.valueOf(src.readString());

        // Read item info.
        int databaseSize = src.readInt();
        mAllItems = new ArrayList<>();
        for (int i = 0; i < databaseSize; ++i)
        {
            Item newItem = null;
            try {
                newItem = new Item(src.readString());

            } catch (FileUtility.InvalidDataFormatException ex) {
                Log.e(this.getClass().getSimpleName(), String.format("Invalid Data Format: %s", ex.InvalidData));
            }
            mAllItems.add(newItem);
        }
    }

    /**
     * Add an item
     * @param item
     */
    public void Insert(Item item)
    {
        mAllItems.add(item);
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
        if (index >=0 && index < mAllItems.size())
        {
            return mAllItems.get(index);
        }
        return null;
    }

    public String name() { return mName; }

    /**
     * Get the corresponding URL of this database (aka. key of this database)
     * @return
     */
    public String url() { return mRefUrl; }

    /**
     * Get season ID of this database
     * @return
     */
    public SeasonID seasonId() { return mSeasonID; }

    /**
     * Get the count of all items
     * @return
     */
    public int size()
    {
        return mAllItems.size();
    }

    @Override
    public boolean equals(Object obj) {
        Database otherDatabase = (Database) obj;

        // Compare based on (mName OR mRefUrl AND mSeasonID)
        boolean isSameName = otherDatabase.mName.equals(this.mName);
        boolean isSameRefUrl = otherDatabase.mRefUrl.equals(this.mRefUrl);
        boolean isSameSeasonID = otherDatabase.mSeasonID.equals(this.mSeasonID);

        /*
         * NOTE: If two series are different, they must have different RefUrls or different Names.
         */
        return (isSameRefUrl || isSameName) && isSameSeasonID;
    }

    @Override
    public Iterator<Item> iterator() {
        return mAllItems.iterator();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write basic info.
        dest.writeString(mName);
        dest.writeString(mRefUrl);
        dest.writeString(mSeasonID.toString());

        // Write item info.
        dest.writeInt(mAllItems.size());
        for (Item item: mAllItems) {
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
