package cn.prichan.Crawler.database;

import java.util.ArrayList;
import java.util.Iterator;

public class DataBase implements Iterable<Item> {

    private String _name;   // Name of this series
    private String _refUrl; // Referred URL
    private ArrayList<Item> _allItems; // All items

    public DataBase(String name, String refUrl)
    {
        // Init.
        _name = name;
        _refUrl = refUrl;

        _allItems = new ArrayList<>();
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

    public int size()
    {
        return _allItems.size();
    }

    @Override
    public Iterator<Item> iterator() {
        return _allItems.iterator();
    }

}
