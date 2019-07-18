package com.swws.marklang.prc_cardbook.activity.update;

import android.util.Log;
import android.util.Pair;

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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DatabaseUpdater2 extends DatabaseUpdaterBase implements IDatabaseUpdater {

    // All pages that will be ignored in _getUrlDict()
    private HashSet<String> IGNORE_PAGES;

    // All constant strings
    private final String URL_PREFIX = "https://prichan.jp/item/";
    private final String RESOURCE_PREFIX = "https://prichan.jp/item/";
    private final String ENTRY_PAGE = "index.html";
    private final String SERIES_ENTRY_PAGE = "index.html";
    private final String JP_KEYWORD_COLOR = "カラー";
    private final String JP_KEYWORD_BRAND = "ブランド";
    private final String JP_KEYWORD_TYPE = "タイプ";
    private final String JP_KEYWORD_RARITY = "レアリティ";
    private final String JP_KEYWORD_SCORE = "いいね☆";

    // Separator used to split Urls
    private static final String mUrlSeparator = ";";
    // Category keyword dictionary
    private Set<Map.Entry<String, String>> JP_CATEGORY_KEYWORDS_ENTRY_SET;

    // Latest series
    private Pair<String, String> mLatestSeries = null;


    public DatabaseUpdater2(DatabaseUpdateDownloadTask downloadTask) {
        super(downloadTask);

        // Init. IGNORE_PAGES
        IGNORE_PAGES = new HashSet<>();
        IGNORE_PAGES.add("item_fc.html"); // Follow ticket has different format

        // Init. JP_CATEGORY_KEYWORDS_ENTRYSET
        Hashtable<String, String> categoryKeywordsHashTable = new Hashtable<>();
        categoryKeywordsHashTable.put("ヘアアクセ", "ヘアアクセ");
        categoryKeywordsHashTable.put("トップス", "トップス");
        categoryKeywordsHashTable.put("スカート", "スカート");
        categoryKeywordsHashTable.put("シューズ", "シューズ");
        categoryKeywordsHashTable.put("ワンピ", "ワンピース");
        /*
         * New Added Keywords on June 17th, 2019
         */
        categoryKeywordsHashTable.put("ドレス", "ワンピース");
        categoryKeywordsHashTable.put("ブラウス", "トップス");
        categoryKeywordsHashTable.put("パンプス", "シューズ");
        categoryKeywordsHashTable.put("セットアップ", "ワンピース");
        categoryKeywordsHashTable.put("サングラス", "ヘアアクセ");
        categoryKeywordsHashTable.put("パンツ", "スカート");
        categoryKeywordsHashTable.put("ジャケット", "トップス");
        categoryKeywordsHashTable.put("ブーツ", "シューズ");
        categoryKeywordsHashTable.put("スニーカー", "シューズ");
        categoryKeywordsHashTable.put("カチューシャ", "ヘアアクセ");
        JP_CATEGORY_KEYWORDS_ENTRY_SET = categoryKeywordsHashTable.entrySet();

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

        /*
         * If no database is required to update,
         * forcedly update the latest database.
         */
        if (rawUrlDict.isEmpty()) {
            if (mLatestSeries != null) {
                rawUrlDict.put(mLatestSeries.first, mLatestSeries.second);
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

        return getItemImages(RESOURCE_PREFIX, SeasonID.SEASON_2ND, databases);
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
            String absoluteURL = URL_PREFIX + relativeUrl;
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
                    if (nodeAttrName.contains(JP_KEYWORD_COLOR)) {
                        // 4. Color
                        item.Color = otherAttr.selectFirst("td").text();

                    } else if (nodeAttrName.contains(JP_KEYWORD_BRAND)) {
                        // 5. Brand
                        if (otherAttr.selectFirst("img") != null) {
                            // Brand is displayed by image
                            item.Brand = otherAttr.selectFirst("img").attr("src");
                        } else {
                            // Brand is displayed by text
                            item.Brand = otherAttr.selectFirst("td").text();
                        }

                    } else if (nodeAttrName.contains(JP_KEYWORD_TYPE)) {
                        // 6. Type
                        item.Type = otherAttr.selectFirst("img").attr("src");

                    } else if (nodeAttrName.contains(JP_KEYWORD_RARITY)) {
                        // 7. Rarity
                        item.Rarity = otherAttr.selectFirst("td").text();

                    } else if (nodeAttrName.contains(JP_KEYWORD_SCORE)) {
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
                item.Category = "";
                for (Map.Entry<String, String> categoryKeywordsEntry:
                        JP_CATEGORY_KEYWORDS_ENTRY_SET) {
                    if (item.ItemName.contains(categoryKeywordsEntry.getKey()))
                    {
                        // A match is found in the pre-built lookup table
                        item.Category = categoryKeywordsEntry.getValue();
                        break;
                    }
                }
                if (item.Category.equals("")) {
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

        // Url dictionary & queue
        LinkedHashMap<String, String> urlDict = new LinkedHashMap<>();
        LinkedList<Pair<String, String>> urlQueue = new LinkedList<>();

        // Get the series page
        String absoluteURL = URL_PREFIX + ENTRY_PAGE;
        String subpageHtmlContent = mHttpUtility.GetHtmlContent(absoluteURL);
        // Parse
        Document pageDoc = Jsoup.parse(subpageHtmlContent);

        // Select the series area
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
                    url = url.concat(SERIES_ENTRY_PAGE);
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

            // Save the current <url, name> pair to "urlDict" and "urlQueue"
            String urlTable = urlTableString.toString();
            if (urlTable.length() > 0) {
                // Remove the trailing mUrlSeparator
                urlTable = urlTable.substring(0, urlTable.length() - 1);

                // 1. Put the current <url, name> pair to "urlDict"
                urlDict.put(urlTable, title);
                // 2. Enqueue the current <url, name> pair
                Pair<String, String> currentPair = new Pair<>(urlTable, title);
                urlQueue.addLast(currentPair);
            }
        }

        // Update mLatestSeries
        if (!urlQueue.isEmpty()) {
            mLatestSeries = urlQueue.getFirst();

        } else {
            mLatestSeries = null;
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
