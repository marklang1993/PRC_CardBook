package cn.prichan.Crawler.utility;

import cn.prichan.Crawler.database.DataBase;
import cn.prichan.Crawler.database.Item;
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
    public final String URL_PREFIX = "https://prichan.jp/items/";
    public final String URL_RES_PREFIX = "https://prichan.jp";
    public final String ENTRY_URL = URL_PREFIX + "1st.html";
    public final String JP_CATEGORY = "カテゴリー";
    public final String JP_COLOR = "カラー";
    public HashSet<String> IGNORE_PAGES;

    /**
     * Status of Downloading
     */
    public static enum DOWNLOAD_STATUS
    {
        OK,
        ERROR,
        EXIST
    }

    /**
     * Constructor
     */
    public HttpUtility()
    {
        // Init. IGNORE_PAGES
        IGNORE_PAGES = new HashSet<>();
        IGNORE_PAGES.add("index.html"); // Duplication
        IGNORE_PAGES.add("ticket.html"); // Follow ticket has different format
    }

    /**
     * Get HTML Content String from URL
     * @param URLString
     * @return contents; null once Error occurs
     */
    public String GetHtmlContent(String URLString)
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
                System.err.println(String.format("Sever Error with RC: %d", responseCode));
                return null;
            }

        } catch (MalformedURLException ex) {
            System.err.println("URL Parse Failed.");
            return null;

        } catch (IOException ex) {
            System.err.println("Connection Failed.");
            return null;
        }
    }

    /**
     * Get the dictionary of all urls
     * @param isPrint enable printing?
     * @return dictionary of all pairs of (text, link)
     */
    public LinkedHashMap<String, String> GetUrlDict(Boolean isPrint)
    {
        // NOTE: Key is "link"
        LinkedHashMap<String, String> urlTable = new LinkedHashMap<>();

        String htmlContent = GetHtmlContent(ENTRY_URL);
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
                if (IGNORE_PAGES.contains(IGNORE_PAGES)) continue;
                // Check duplicated pages
                if (urlTable.containsKey(link)) continue;

                // Output
                if (isPrint)
                {
                    System.out.println(String.format("%s, %s", text, link));

                }

                // Insert
                urlTable.put(link, text);
            }
        }
        return urlTable;
    }

    /**
     * Populate all items from a given URL
     * @param name
     * @param relativeUrl
     * @return
     */
    public DataBase PopulateAllItems(String name, String relativeUrl)
    {
        DataBase dataBase = new DataBase(name, relativeUrl);
        String urlString = URL_PREFIX + relativeUrl;
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
                System.err.println(String.format(
                        "Duplicated subpage: \"%s\" at \"%s\"",
                        subpage, relativeUrl));
                continue;
            }

        }

        // Download next page for details
        for (String subpage: subpages)
        {
            String subpageUrlString = URL_PREFIX + subpage;
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
                if (detailNodeTitle.equals(JP_CATEGORY)) {
                    // 4. Category
                    localItem.Category = detailNode.select("div[class=-value]").first().text();
                } else if (detailNodeTitle.equals(JP_COLOR)) {
                    // 5. Color
                    localItem.Color = detailNode.select("div[class=-value]").first().text();
                } else {
                    // ERROR: Unknown type detail node
                    System.err.println(String.format(
                            "Detail Node with Unknown Type: \"%s\" at \"%s\" with type \"%s\"",
                            subpage, relativeUrl, detailNodeTitle));
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
                    System.err.println(String.format(
                            "Status Node with Unknown Type: \"%s\" at \"%s\" with type \"%s\"",
                            subpage, relativeUrl, statusClassName));
                }

            }

            // Insert
            System.out.println(localItem.toString());
            dataBase.Insert(localItem);
        }
        return dataBase;
    }

    /**
     * Download file from given URL to given destination
     * @param relativeUrl
     * @param directory
     * @param target
     * @return
     */
    public DOWNLOAD_STATUS Download(String relativeUrl, File directory, File target)
    {
        try
        {
            // Check
            if (!directory.exists())
            {
                // Directory does NOT exist
                System.err.println("Directory does not exist.");
                return DOWNLOAD_STATUS.ERROR;
            }
            if (target.exists())
            {
                // File exist
                return DOWNLOAD_STATUS.EXIST;
            }

            // Download
            URL URLObject = new URL(URL_RES_PREFIX + relativeUrl);
            FileUtils.copyURLToFile(URLObject, target);

        } catch (MalformedURLException ex) {
            System.err.println(String.format("URL Parse Failed: %s", relativeUrl));
            return DOWNLOAD_STATUS.ERROR;

        } catch (IOException ex) {
            System.err.println(String.format("Connection Failed: %s", target.getName()));
            return DOWNLOAD_STATUS.ERROR;
        }

        return DOWNLOAD_STATUS.OK;
    }
}
