package com.swws.marklang.prc_cardbook.activity.update;

import android.util.Log;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.HttpUtility;
import com.swws.marklang.prc_cardbook.utility.MathUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class DatabaseUpdater2 extends DatabaseUpdaterBase implements IDatabaseUpdater {

    // Separator used to split Urls
    private static final String mUrlSeparator = ";";

    // All pages that will be ignored in _getUrlDict()
    private HashSet<String> IGNORE_PAGES;

    public DatabaseUpdater2(DatabaseUpdateDownloadTask downloadTask) {
        super(downloadTask);

        // Init. IGNORE_PAGES
        IGNORE_PAGES = new HashSet<>();
        IGNORE_PAGES.add("item_fc.html"); // Follow ticket has different format
    }

    @Override
    public LinkedHashMap<String, String> GetUrlDict(ArrayList<Database> oldDatabases)
            throws HttpUtility.ServerErrorException, IOException {

        // Update the initial value of the progress bar
        mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(mProgressValues[0],
                mContext.getString(R.string.info_download_pages_list))
        );

        // Get new Url Dictionary
        LinkedHashMap<String, String> rawUrlDict = _populateAllSeries();

        // Filter out the duplicated url based on the "oldDatabases".
        if (oldDatabases != null) {
            Iterator<Database> oldDatabasesIterator = oldDatabases.iterator();
            while (oldDatabasesIterator.hasNext()) {
                Database oldEntry = oldDatabasesIterator.next();

                // If this entry belongs to 2ND SEASON
                if (oldEntry.seasonId() == SeasonID.SEASON_2ND) {
                    // Get Corresponding URL
                    String correspondingUrl = oldEntry.url();
                    if (rawUrlDict.containsKey(correspondingUrl)) { // This entry is in the rawUrlDict
                        rawUrlDict.remove(correspondingUrl);
                    }
                }
            }
        }

        return rawUrlDict;
    }

    @Override
    public LinkedList<Database> GetDatabaseLinkedList(LinkedHashMap<String, String> urlDict)
            throws HttpUtility.ServerErrorException, IOException {

        // Update the initial value of the progress bar
        mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(mProgressValues[1],
                mContext.getString(R.string.info_download_coordinates))
        );

        // Retrieve each database from website based on "urlDict"
        LinkedList<Database> databases = new LinkedList<>();
        int countDatabases = urlDict.size();
        Iterator<Map.Entry<String, String>> iterator = urlDict.entrySet().iterator();
        for (int i = 0; i < countDatabases; ++i)
        {
            // ## Check whether this task is cancelled.
            if (mDownLoadTask.GetCancelStatus()) {
                return null;
            }

            // Get the current entry for retrieving the url of the series page.
            Map.Entry<String, String> urlEntry = iterator.next();
            String seriesRelativeUrl = urlEntry.getKey();
            String seriesName = urlEntry.getValue();

            // Update the value of the progress bar
            int currentProgress = MathUtility.calculateCurrentProgressValue(
                    i, 0, countDatabases, mProgressValues[1], mProgressValues[2] - 1
            );
            mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(currentProgress,
                    mContext.getString(R.string.info_download_coordinates) + " - " + seriesName
                    )
            );

            // Generate the Database for the current series
            Database dataBase = new Database(seriesName, seriesRelativeUrl, SeasonID.SEASON_2ND);

            // Populate all items
            _populateAllItems(dataBase);

            // Add this Database
            databases.add(dataBase);
        }
        return databases;
    }

    /**
     * Get all images of all items in Season 2
     * @param databases
     * @return
     * @throws HttpUtility.DirCreateException
     * @throws IOException
     */
    @Override
    public boolean GetItemImages(LinkedList<Database> databases)
            throws HttpUtility.DirCreateException, IOException {

        String urlPrefix = mContext.getString(R.string.database_updater2_resource_prefix);

        return getItemImages(urlPrefix, SeasonID.SEASON_2ND, databases);
    }


    /**
     * Populate all items and add them to the given database.
     * @param database
     */
    private void _populateAllItems(Database database)
            throws HttpUtility.ServerErrorException, IOException {

        // Get each URL
        ArrayList<String> urlList = GetUrlList(database.url());

        for (String relativeUrl : urlList) {
            // Get the item page
            String absoluteURL =
                    mContext.getString(R.string.database_updater2_url_prefix) + relativeUrl;
            String subpageHtmlContent = mHttpUtility.GetHtmlContent(absoluteURL);
            // Parse
            Document pageDoc = Jsoup.parse(subpageHtmlContent);

            // Selete the item area
            Elements itemElements = pageDoc.select("div[class=modalWindow]");
            Elements itemDetailElements = itemElements.select("dl[class=modalDetail]");

            // Iterate all items
            for (Element itemDetail: itemDetailElements) {

                // Retrieve
                Item item = new Item();

                // 1. Name
                Element nameNode = itemDetail.selectFirst("dd[class=itemName]");
                item.ItemName = nameNode.text();

                // 2. Internal ID
                Element idNode = itemDetail.selectFirst("dd[class=itemCode]");
                item.InternalID = idNode.text();

                // 3. Image
                Element imageNode = itemDetail.selectFirst("img");
                item.ItemImage = imageNode.attr("src");

                // Extract other attributes
                Elements otherAttrs = itemDetail.select("dd[class=itemSpec]");
                otherAttrs = otherAttrs.select("tr");
                for (Element otherAttr: otherAttrs) {

                    // Pick up each Attribute
                    Element nodeAttr = otherAttr.selectFirst("th");
                    if (nodeAttr == null) continue; // Empty node, skip

                    String nodeAttrName = nodeAttr.text();
                    if (nodeAttrName.contains(
                            mContext.getString(R.string.database_updater2_jp_keyword_color))) {
                        // 4. Color
                        item.Color = otherAttr.selectFirst("td").text();

                    } else if (nodeAttrName.contains(
                            mContext.getString(R.string.database_updater2_jp_keyword_brand))) {
                        // 5. Brand
                        if (otherAttr.selectFirst("img") != null) {
                            // Brand is displayed by image
                            item.Brand = otherAttr.selectFirst("img").attr("src");
                        } else {
                            // Brand is displayed by text
                            item.Brand = otherAttr.selectFirst("td").text();
                        }

                    } else if (nodeAttrName.contains(
                            mContext.getString(R.string.database_updater2_jp_keyword_type))) {
                        // 6. Type
                        item.Type = otherAttr.selectFirst("img").attr("src");

                    } else if (nodeAttrName.contains(
                            mContext.getString(R.string.database_updater2_jp_keyword_rarity))) {
                        // 7. Rarity
                        item.Rarity = otherAttr.selectFirst("td").text();

                    } else if (nodeAttrName.contains(
                            mContext.getString(R.string.database_updater2_jp_keyword_score))) {
                        // 8. Score
                        item.Score = otherAttr.selectFirst("td").text();

                    } else {
                        // ERROR: Unknown type detail node
                        Log.e(this.getClass().getSimpleName(), String.format(
                                "Attribute Node with Unknown Type: %s at %s", nodeAttrName, database.name()));
                    }
                }

                // 9. Category
                /**
                 * Since the web page does not give out the item category,
                 * it is necessary to extract from the item name
                 */
                if (item.ItemName.contains(
                        mContext.getString(R.string.database_updater2_jp_category_hair_accessory))) {
                    // Hair Accessory
                    item.Category = mContext.getString(R.string.database_updater2_jp_category_hair_accessory);

                } else if (item.ItemName.contains(
                        mContext.getString(R.string.database_updater2_jp_category_tops))) {
                    // Tops
                    item.Category = mContext.getString(R.string.database_updater2_jp_category_tops);

                } else if (item.ItemName.contains(
                        mContext.getString(R.string.database_updater2_jp_category_skirt))) {
                    // Skirt
                    item.Category = mContext.getString(R.string.database_updater2_jp_category_skirt);

                } else if (item.ItemName.contains(
                        mContext.getString(R.string.database_updater2_jp_category_shoes))) {
                    // Shoes
                    item.Category = mContext.getString(R.string.database_updater2_jp_category_shoes);

                } else if (item.ItemName.contains(
                        mContext.getString(R.string.database_updater2_jp_category_one_piece))) {
                    // One Piece
                    item.Category = mContext.getString(R.string.database_updater2_jp_category_one_piece);

                } else {
                    item.Category = "UNKNOWN";
                }

                // Add this item to the Database
                database.Insert(item);
            }
        }
    }


    /**
     * Populate all series
     *
     * NOTE: This URL string in the result LinkedHashMap will have the format of
     * "URL1<mUrlSeparator>URL2<mUrlSeparator>..."
     */
    private LinkedHashMap<String, String> _populateAllSeries()
            throws HttpUtility.ServerErrorException, IOException {

        LinkedHashMap<String, String> urlDict = new LinkedHashMap<>();

        // Get the series page
        String absoluteURL =
                mContext.getString(R.string.database_updater2_url_prefix) +
                        mContext.getString(R.string.database_updater2_entry_page);
        String subpageHtmlContent = mHttpUtility.GetHtmlContent(absoluteURL);
        // Parse
        Document pageDoc = Jsoup.parse(subpageHtmlContent);

        // Selete the series area
        Elements seriesArea = pageDoc.select("div[class=section clearfix itemListWrap]");
        Elements seriesList = seriesArea.select("div[class=itemIndexList]");

        // Populate each series
        for (Element seriesElement: seriesList) {
            // Get series title as database name
            String title = seriesElement.select("h3").text();

            // Get corresponding urls
            Elements urlList = seriesElement.select("a");
            StringBuilder urlTableString = new StringBuilder();
            HashSet<String> setToRemoveDuplication = new HashSet<>();

            for (Element urlElement: urlList) {
                // Populate each URL
                String url = urlElement.attr("href");

                // Check is this URL in the IGNORE_PAGES
                boolean isIgnore = false;
                for (String ignoreUrl: IGNORE_PAGES) {
                    isIgnore |= url.contains(ignoreUrl);
                }
                if (isIgnore) continue;

                // Remove the "?" GET request
                int requestStringIndex = url.indexOf("?");
                if (requestStringIndex > 0) {
                    url = url.substring(0, requestStringIndex);
                }

                // Check if ".html" is presented
                if (url.indexOf(".html") <= 0) {
                    // Manually add the entry page
                    url = url.concat(mContext.getString(R.string.database_updater2_series_entry_page));
                }

                // Add the HashSet for duplication removing
                if (!setToRemoveDuplication.contains(url)) {
                    // Add the current url to the HashSet
                    setToRemoveDuplication.add(url);
                    // Add the current url to the urlTableString
                    urlTableString.append(url);
                    urlTableString.append(mUrlSeparator);
                }
            }

            String urlTable = urlTableString.toString();
            if (urlTable.length() > 0) {
                // Remove the trailing mUrlSeparator
                urlTable = urlTable.substring(0, urlTable.length() - 1);
                // Add the current <url, name> pair to the urlDict.
                urlDict.put(urlTable, title);
            }
        }

        return urlDict;
    }


    /**
     * Get urlList from given urlTableString
     * @return
     */
    public static ArrayList<String> GetUrlList(String urlTableString) {

        String[] urlArray = urlTableString.split(mUrlSeparator);

        return new ArrayList<>(Arrays.asList(urlArray));
    }
}
