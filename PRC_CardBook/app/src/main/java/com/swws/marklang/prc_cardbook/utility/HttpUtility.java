package com.swws.marklang.prc_cardbook.utility;

import android.content.Context;
import android.util.Log;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class HttpUtility {

    private Context mContext;
    private HashSet<String> IGNORE_PAGES;

    /**
     * (Official) Server Error Exception
     */
    public static class ServerErrorException extends Exception {
        public String mErrorMessage;

        public ServerErrorException(String errorMessage)
        {
            super();
            mErrorMessage = errorMessage;
        }
    }

    /**
     * Directory Creation Error Exception
     */
    public static class DirCreateException extends Exception {
        public String mDirectory;

        public DirCreateException(String directory)
        {
            super();
            mDirectory = directory;
        }
    }

    /**
     * Constructor
     */
    public HttpUtility(Context context)
    {
        mContext = context;

        // Init. IGNORE_PAGES
        IGNORE_PAGES = new HashSet<>();
        IGNORE_PAGES.add("index.html"); // Duplication
        IGNORE_PAGES.add("promotion.html"); // Duplication
        IGNORE_PAGES.add("ticket.html"); // Follow ticket has different format
    }

    /**
     * Get HTML Content String from URL
     * @param URLString
     * @return contents; null once Error occurs
     */
    public String GetHtmlContent(String URLString) throws ServerErrorException, IOException
    {
        try {
            URL URLObject = new URL(URLString);
            HttpURLConnection connection = (HttpURLConnection)URLObject.openConnection();
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200)
            {
                // Read contents
                StringBuilder sb = new StringBuilder();
                InputStream inputStream = connection.getInputStream();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                    sb.append('\n');
                }
                in.close();

                return sb.toString();

            } else {
                // Server Error
                String errorMessage = String.format("URL: %s\n Sever Error with RC: %d", URLString, responseCode);
                Log.e(this.getClass().getSimpleName(), errorMessage);
                throw new ServerErrorException(errorMessage);
            }

        } catch (MalformedURLException ex) {
            // URL format error
            Log.e(this.getClass().getSimpleName(), "URL Parse Failed.");
            throw ex;

        } catch (IOException ex) {
            // Network error
            Log.e(this.getClass().getSimpleName(), "Connection Failed.");
            throw ex;
        }
    }

    /**
     * Get the dictionary of all urls
     * @param isPrint enable printing?
     * @return dictionary of all pairs of (text, link)
     */
    public LinkedHashMap<String, String> GetUrlDict(Boolean isPrint) throws ServerErrorException, IOException
    {
        // NOTE: Key is "link"
        LinkedHashMap<String, String> urlTable = new LinkedHashMap<>();

        String htmlContent = GetHtmlContent(
                mContext.getString(R.string.http_utility_url_prefix) +
                        mContext.getString(R.string.http_utility_entry_page)
        );
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
     * @throws ServerErrorException
     * @throws IOException
     */
    public LinkedList<String> GetAllItemSubpageUrls(String seriesRelativeUrl) throws ServerErrorException, IOException
    {
        String urlString = mContext.getString(R.string.http_utility_url_prefix) + seriesRelativeUrl;
        String htmlContent = GetHtmlContent(urlString);

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
    public Item PopulateItems(
            String seriesRelativeUrl, String itemSubpageUrl, boolean isPrint)
            throws ServerErrorException, IOException
    {
        // Get "detail page"
        String subpageUrlString = mContext.getString(R.string.http_utility_url_prefix) + itemSubpageUrl;
        String subpageHtmlContent = GetHtmlContent(subpageUrlString);
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
            if (detailNodeTitle.equals(mContext.getString(R.string.http_utility_jp_keyword_category))) {
                // 4. Category
                localItem.Category = detailNode.select("div[class=-value]").first().text();
            } else if (detailNodeTitle.equals(mContext.getString(R.string.http_utility_jp_keyword_color))) {
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

    /**
     * Download file from given URL to given destination
     * @param relativeUrl
     * @param directory
     * @param isPrint
     * @throws DirCreateException
     * @throws IOException
     */
    public void Download(String relativeUrl, String directory, boolean isPrint)
            throws DirCreateException, IOException
    {
        String URLLink = mContext.getString(R.string.http_utility_resource_prefix) + relativeUrl;

        try
        {
            // Check the existence of the directory
            File dirFile = new File(directory);
            if (!dirFile.exists()) {
                // Directory does NOT exist
                Log.e(this.getClass().getSimpleName(), String.format("Directory %s does not exist.", dirFile.getName()));

                // Create a new directory
                if (dirFile.mkdir()) {
                    Log.e(this.getClass().getSimpleName(), String.format("Directory %s created.", dirFile.getName()));

                } else {
                    Log.e(this.getClass().getSimpleName(), String.format("Directory %s cannot be created.", dirFile.getName()));
                    throw new DirCreateException(dirFile.getName());
                }
            }

            // Check the existence of the target file
            String fileName = (new File(relativeUrl)).getName();
            File target = new File(directory + fileName);
            if (target.exists()) {
                // File existed - Skip
                if (isPrint) {
                    Log.i(this.getClass().getSimpleName(), String.format("File: %s - EXIST", fileName));
                }
                return;
            }

            // Download
            URL URLObject = new URL(URLLink);
            FileUtils.copyURLToFile(URLObject, target);
            // File is downloaded
            if (isPrint) {
                Log.i(this.getClass().getSimpleName(), String.format("File: %s - OK", fileName));
            }

        } catch (MalformedURLException ex) {
            Log.e(this.getClass().getSimpleName(), String.format("URL Parse Failed: %s", URLLink));
            throw ex;

        } catch (IOException ex) {
            Log.e(this.getClass().getSimpleName(), String.format("Connection Failed: %s", URLLink));
            throw ex;
        }
    }
}
