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

public class HttpUtility {
    public final String URL_PREFIX = "https://prichan.jp/items/";
    public final String ENTRY_URL = URL_PREFIX + "1st.html";
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
        // Select all items
        Elements categoryListElements = doc.select(
                "div[class=mainAreaR]").select(
                        "div[class=categoryDetailList]"
        );
        Elements allElements = categoryListElements.first().children();

        // Iterate all children node to retrieve data
        for (Element e:
                allElements) {

            Item localItem = new Item();
            // Retrieve

            // 1. Image
            localItem.ItemImage = e.select("p[class=itemimg]").
                    select("img").
                    attr("src");

            // 2. Name Data
            Element itemNameElement = e.select("div[class=itemName]").first();
            localItem.InternalID = itemNameElement.select("h2").select("span").text();
            String rawItemName = itemNameElement.select("h2").text();
            if (localItem.InternalID.length() == 0)
            {
                // InternalID does not exist
                localItem.ItemName = rawItemName;

            } else {
                // InternalID is required to be removed
                localItem.ItemName = rawItemName.substring(localItem.InternalID.length() + 1); // NOTE: +1 due to the redundant space
            }

            // 3. Attributes
            Elements itemAttributesRaw = e.select("table").select("tbody").first().children();
            Elements itemAttribute1 = itemAttributesRaw.get(1).children(); // Category, Type, Brand
            localItem.Category = itemAttribute1.get(0).text();
            localItem.Type = itemAttribute1.get(1).text();
            localItem.Brand = itemAttribute1.get(2).select("img").attr("src");
            Elements itemAttribute2 = itemAttributesRaw.get(3).children(); // Rarity, Score, Color
            localItem.Rarity = itemAttribute2.get(0).text();
            localItem.Score = itemAttribute2.get(1).text();
            localItem.Color = itemAttribute2.get(2).text();

            // 4. Remarks
            localItem.Remarks = e.select("div[class=read]").select("p").first().text();

            // Insert
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
            URL URLObject = new URL(URL_PREFIX + relativeUrl);
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
