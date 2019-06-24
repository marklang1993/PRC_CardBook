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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class DatabaseUpdater1 extends DatabaseUpdaterBase implements IDatabaseUpdater {

    // All pages that will be ignored in _getUrlDict()
    private HashSet<String> IGNORE_PAGES;

    // All constant strings
    /*
    <string name="database_updater1_url_prefix">https://prichan.jp/season1/items/</string>
    <string name="database_updater1_resource_prefix">https://prichan.jp</string>
    <string name="database_updater1_entry_page">1st.html</string>
    <string name="database_updater1_jp_keyword_category">カテゴリー</string>
    <string name="database_updater1_jp_keyword_color">カラー</string>
    */

    private final String URL_PREFIX = "https://prichan.jp/season1/items/";
    private final String RESOURCE_PREFIX = "https://prichan.jp";
    private final String ENTRY_PAGE = "1st.html";
    private final String JP_KEYWORD_CATEGORY = "カテゴリー";
    private final String JP_KEYWORD_COLOR = "カラー";


    /**
     * Constructor
     * @param downloadTask Caller (Async Task)
     */
    public DatabaseUpdater1(DatabaseUpdateDownloadTask downloadTask) {
        super(downloadTask);

        // Init. IGNORE_PAGES
        IGNORE_PAGES = new HashSet<>();
        IGNORE_PAGES.add("index.html"); // Duplication
        IGNORE_PAGES.add("promotion.html"); // Duplication
        IGNORE_PAGES.add("ticket.html"); // Follow ticket has different format
    }

    /**
     * Get the dictionary of all urls
     * @param oldDatabases Old Databases
     * @return
     * @throws HttpUtility.ServerErrorException
     * @throws IOException
     */
    @Override
    public LinkedHashMap<String, String> GetUrlDict(ArrayList<Database> oldDatabases)
        throws HttpUtility.ServerErrorException, IOException {

        // Update the initial value of the progress bar
        mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(mProgressValues[0],
                mContext.getString(R.string.info_download_pages_list))
        );

        // Retrieve the raw dictionary from the official website
        LinkedHashMap<String, String> rawUrlDict = _getUrlDict(mIsPrintDebug);

        // Filter out the duplicated url based on the "oldDatabases".
        if (oldDatabases != null) {
            /**
             * Iterate all entries in the "oldDatabases" and check
             * (Weak) Assert: this entry is in the rawUrlDict
             * NOTE: Even though this entry is not in the rawUrlDict, the following
             *       operations will not be affected. That's why I use "If" here.
             *
             * Algorithm:
             * If this entry has the same item size as the entry in the rawUrlDict,
             * then remove this entry from the rawUrlDict.
             */
            Iterator<Database> oldDatabasesIterator = oldDatabases.iterator();
            while (oldDatabasesIterator.hasNext()) {
                Database oldEntry = oldDatabasesIterator.next();

                // If this entry belongs to 1ST SEASON
                if (oldEntry.seasonId() == SeasonID.SEASON_1ST) {
                    // Get Corresponding URL
                    String correspondingUrl = oldEntry.url();

                    if (rawUrlDict.containsKey(correspondingUrl)) { // This entry is in the rawUrlDict
                        // Get the up-to-date item size of this entry
                        LinkedList<String> allItemSubpageUrls = _getAllItemSubpageUrls(correspondingUrl);
                        int newSize = allItemSubpageUrls.size();
                        int oldSize = oldEntry.size();
                        if (newSize == oldSize) { // This entry does not change at all
                            // Remove this entry from the update list
                            rawUrlDict.remove(correspondingUrl);
                        }
                    }
                }
            }
        }

        return rawUrlDict;
    }

    /**
     *
     * @param urlDict
     * @return
     * @throws HttpUtility.ServerErrorException
     * @throws IOException
     */
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

            // Get the urls of the subpage of all items
            LinkedList<String> itemSubpageUrls = _getAllItemSubpageUrls(seriesRelativeUrl); // TODO: try to eliminate this

            // Generate the Database for the current series
            Database dataBase = new Database(seriesName, seriesRelativeUrl, SeasonID.SEASON_1ST);
            for (String itemSubpageUrl: itemSubpageUrls) {
                // ## Check whether this task is cancelled.
                if (mDownLoadTask.GetCancelStatus()) {
                    return null;
                }

                // Get 1 item of this series
                Item item = _populateItems(seriesRelativeUrl, itemSubpageUrl, mIsPrintDebug);
                dataBase.Insert(item);
            }

            // Add this Database
            databases.add(dataBase);
        }
        return databases;
    }

    /**
     * Get all images of all items in Season 1
     * @param databases
     * @return
     * @throws HttpUtility.DirCreateException
     * @throws IOException
     */
    @Override
    public boolean GetItemImages(LinkedList<Database> databases)
            throws HttpUtility.DirCreateException, IOException
    {
        return getItemImages(RESOURCE_PREFIX, SeasonID.SEASON_1ST, databases);
    }

    /**
     * Retrieve the urls of all series
     * @param isPrint enable printing?
     * @return dictionary of all pairs of (text, link)
     */
    private LinkedHashMap<String, String> _getUrlDict(Boolean isPrint) throws HttpUtility.ServerErrorException, IOException
    {
        // NOTE: Key is "link"
        LinkedHashMap<String, String> urlTable = new LinkedHashMap<>();

        String htmlContent = mHttpUtility.GetHtmlContent(URL_PREFIX + ENTRY_PAGE);
        Document doc = Jsoup.parse(htmlContent);
        // Select the main area
        Elements mainAreaItems = doc.select("nav[class=items-nav]");
        mainAreaItems = mainAreaItems.select("li");

        // Iterate the main area and extract each element
        for (Element liRow: mainAreaItems)
        {
            // Get all possible hyper links from liRow
            Elements rawHyperLinks = liRow.select("a");
            for (Element aRow: rawHyperLinks)
            {
                // Get linked page name
                String link = aRow.attr("href");
                // Extract name of the corresponding series
                Element spanRow = aRow.select("span").first();
                spanRow.select("rt").remove(); // Remove nodes that represent "furigana"
                String text = spanRow.text();

                // Filtering by IGNORE_PAGE
                if (IGNORE_PAGES.contains(link)) continue;
                // Check duplicated pages
                if (urlTable.containsKey(link)) continue;

                // Output
                if (isPrint) {
                    Log.i(this.getClass().getSimpleName(), String.format("%s, %s", text, link));

                }

                // Insert
                urlTable.put(link, text);
            }
        }
        return urlTable;
    }

    /**
     * Get a list of urls of all items in the given series
     * @param seriesRelativeUrl
     * @return
     * @throws HttpUtility.ServerErrorException
     * @throws IOException
     */
    private LinkedList<String> _getAllItemSubpageUrls(String seriesRelativeUrl) throws HttpUtility.ServerErrorException, IOException
    {
        String urlString = URL_PREFIX + seriesRelativeUrl;
        String htmlContent = mHttpUtility.GetHtmlContent(urlString);

        // Parse
        Document doc = Jsoup.parse(htmlContent);

        // Get all subpages
        HashSet<String> subpagesExisted = new HashSet<>(); // Only used for check existence
        LinkedList<String> subpages = new LinkedList<>();
        Elements coordinateLists = doc.select("div[class=coordinate-lists]");
        Elements coordinateHyperLinks = coordinateLists.select("a");

        for (Element e: coordinateHyperLinks) {
            String subpage = e.attr("href");
            // Check duplicated
            if (!subpagesExisted.contains(subpage)) {
                // Add to "subpagesExisted" for duplication checking
                subpagesExisted.add(subpage);
                // Add to "subpages"
                subpages.add(subpage);
            } else {
                // ERROR: Duplicated subpage found
                Log.e(this.getClass().getSimpleName(), String.format(
                        "Duplicated subpage: \"%s\" at \"%s\"",
                        subpage, seriesRelativeUrl)
                );
                continue;
            }

        }
        return subpages;
    }

    /**
     * Populate 1 item data from a given URL
     * @param seriesRelativeUrl
     * @param itemSubpageUrl
     * @param isPrint enable printing?
     * @return
     */
    private Item _populateItems(
            String seriesRelativeUrl, String itemSubpageUrl, boolean isPrint)
            throws HttpUtility.ServerErrorException, IOException
    {
        // Get "detail page"
        String subpageUrlString = URL_PREFIX + itemSubpageUrl;
        String subpageHtmlContent = mHttpUtility.GetHtmlContent(subpageUrlString);
        // Parse
        Document subpageDoc = Jsoup.parse(subpageHtmlContent);

        // Get data area
        Elements detailMainArea = subpageDoc.select("main");
        detailMainArea = detailMainArea.select("div[class=the-item]");

        // Retrieve
        Item localItem = new Item();

        // 1. Name
        Element titleNode = detailMainArea.select("div[class=-title]").first();
        localItem.ItemName = titleNode.text();

        // Pick up 2 nodes in next layer
        Elements thumbNode = detailMainArea.select("div[class=-inner]").select("div[class=-thumb]");
        Elements rightNode = detailMainArea.select("div[class=-inner]").select("div[class=-right]");

        // 2. Internal ID
        localItem.InternalID = thumbNode.select("div[class=-id]").text();

        // 3. Image
        Element imageNode = thumbNode.select("img").first();
        localItem.ItemImage = imageNode.attr("data-src");

        // Populate 2 nodes of details
        Elements detailNodes = rightNode.select("li[class=-detail]");
        for (Element detailNode: detailNodes) {
            Element detailNodeTitleNode = detailNode.select("div[class=-title]").first();
            String detailNodeTitle = detailNodeTitleNode.text();
            if (detailNodeTitle.equals(JP_KEYWORD_CATEGORY)) {
                // 4. Category
                localItem.Category = detailNode.select("div[class=-value]").first().text();
            } else if (detailNodeTitle.equals(JP_KEYWORD_COLOR)) {
                // 5. Color
                localItem.Color = detailNode.select("div[class=-value]").first().text();
            } else {
                // ERROR: Unknown type detail node
                Log.e(this.getClass().getSimpleName(), String.format(
                        "Detail Node with Unknown Type: \"%s\" at \"%s\" with type \"%s\"",
                        itemSubpageUrl, seriesRelativeUrl, detailNodeTitle));
            }
        }

        // 6. Brand
        Element brandNode = rightNode.select("li[class=-detail -brand]").first();
        localItem.Brand = brandNode.select("div[class=-value]")
                .select("img").first().attr("data-src");

        // 7. Type
        Element genreNode = rightNode.select("li[class=-detail -genre]").first();
        localItem.Type = genreNode.select("div[class=-value]")
                .select("img").first().attr("data-src");

        // Populate status
        Elements statusNodes = rightNode.select("li[class=-detail -status]").select("div");
        for (Element statusNode: statusNodes) {
            String statusClassName = statusNode.className();
            if (statusClassName.contains("rarity")) {
                // 8. Rarity
                localItem.Rarity = statusNode.text();
            } else if (statusClassName.contains("like")) {
                // 9. Score
                localItem.Score = statusNode.text();
            } else {
                // ERROR: Unknown type status node
                Log.e(
                        this.getClass().getSimpleName(),
                        String.format("Status Node with Unknown Type: \"%s\" at \"%s\" with type \"%s\"",
                                itemSubpageUrl, seriesRelativeUrl, statusClassName)
                );
            }

        }

        // Output debug info.
        if (isPrint) {
            Log.i(this.getClass().getSimpleName(), localItem.toString());
        }

        return localItem;
    }
}
