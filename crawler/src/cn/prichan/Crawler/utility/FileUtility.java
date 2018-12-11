package cn.prichan.Crawler.utility;

import cn.prichan.Crawler.database.DataBase;
import cn.prichan.Crawler.database.Item;

import java.io.*;
import java.util.*;

public class FileUtility {
    private final String TOP_DIR = "data";
    private final String IMAGE_DIR = TOP_DIR + "/image/";
    private final String BRAND_DIR = TOP_DIR + "/brand/";
    private final String TYPE_DIR = TOP_DIR + "/type/";
    private final String META_FILE = TOP_DIR + "/meta.txt";

    /**
     * Invalid Data Format Exception
     */
    public static class InvalidDataFormatException extends Exception {
        public String InvalidData;

        public InvalidDataFormatException(String invalidData)
        {
            super();
            InvalidData = invalidData;
        }
    }


    private BufferedReader _getReader(String fileName)
    {
        try
        {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            return bufferedReader;
        }
        catch (Exception ex) {
            return null;
        }
    }

    private BufferedWriter _getWriter(String fileName)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            return bufferedWriter;
        }
        catch (Exception ex) {
            return null;
        }
    }

    private void _writeLine(BufferedWriter writer, String line, Boolean isPrint)
    {
        try
        {
            writer.write(line, 0, line.length());
            writer.write('\n');
            if (isPrint)
            {
                System.out.println(line);
            }

        } catch (IOException ex)
        {
            System.err.println(String.format("Write line failed: %s", line));
        }
    }

    private String _readLine(BufferedReader reader, Boolean isPrint)
    {
        try
        {
            String line = reader.readLine();
            if (isPrint)
            {
                System.out.println(line);
            }
            return line;

        } catch (IOException ex) {

            System.err.println(String.format("Read line failed"));
            return null;
        }
    }

    private void _close(Closeable operator)
    {
        try {
            operator.close();
        }
        catch (Exception ex)
        {
            System.err.println("Close operator failed.");
        }
    }

    /**
     * Write all meta data from the website
     * @param httpUtility
     * @param isPrint
     */
    public void WriteAllMetaData(HttpUtility httpUtility, Boolean isPrint)
    {
        // Get url dictionary
        LinkedHashMap<String, String> urlDict = httpUtility.GetUrlDict(false);
        Iterator<Map.Entry<String, String>> iterator = urlDict.entrySet().iterator();

        BufferedWriter bufferedWriter = _getWriter(META_FILE);
        int cursor = 0;
        while (iterator.hasNext())
        {
            Map.Entry<String, String> urlEntry = iterator.next();
            ++cursor;
            _writeLine(bufferedWriter,
                    String.format("%d,%s,%s", cursor, urlEntry.getValue(), urlEntry.getKey()),
                    isPrint
            );

            DataBase dataBase = httpUtility.PopulateAllItems(urlEntry.getValue(), urlEntry.getKey());
            for (Item item:
                    dataBase) {
                _writeLine(bufferedWriter,
                        item.toString(),
                        isPrint
                );
            }

            // Print one more line
            _writeLine(bufferedWriter,
                    "",
                    isPrint
            );
        }

        _close(bufferedWriter);
    }

    /**
     * Read all meta data from META_FILE
     * @param isPrint
     * @return
     */
    public ArrayList<DataBase> ReadAllMetaData(Boolean isPrint)
    {
        BufferedReader bufferedReader = _getReader(META_FILE);
        ArrayList<DataBase> listDataBase = new ArrayList<>();

        // Read data
        try
        {
            DataBase dataBase = null;
            String line = _readLine(bufferedReader, isPrint);
            while(line != null)
            {
                // Process
                if (dataBase == null)
                {
                    // New DataBase is reqired to be created
                    String[] tokens = line.split(",");
                    dataBase = new DataBase(tokens[1], tokens[2]);
                }
                else
                {
                    // Use old dataBase
                    if (line.equals(""))
                    {
                        // Need to create new DataBase in next readLine
                        listDataBase.add(dataBase);
                        dataBase = null;

                    } else {
                        // Process 1 item data
                        String[] tokens = line.split(",", Item.COUNT_ELEMENT);
                        if (tokens.length != Item.COUNT_ELEMENT)
                        {
                            throw new InvalidDataFormatException(line);
                        }

                        dataBase.Insert(
                                tokens[0],
                                tokens[1],
                                tokens[2],
                                tokens[3],
                                tokens[4],
                                tokens[5],
                                tokens[6],
                                tokens[7],
                                tokens[8],
                                tokens[9]
                        );
                    }
                }
                // Read Next Line
                line = bufferedReader.readLine();
            }

        } catch (IOException ex) {
            System.err.println(String.format("Read META_FILE failed"));

        } catch (InvalidDataFormatException ex) {
            System.err.println(String.format(String.format("Invalid Data Format: %s", ex.InvalidData)));
            String[] tokens = ex.InvalidData.split(",");
            System.out.println("End");

        } finally {
            _close(bufferedReader);
        }

        return listDataBase;
    }

    private void _downloadImage(HashSet<String> allImageNames, String relativeUrl, String directory, HttpUtility httpUtility, Boolean isPrint)
    {
        if (!relativeUrl.equals(""))
        {
            // "relativeUrl" must be NOT empty
            String fileName = (new File(relativeUrl)).getName();

            /* For debug purpose */
            if (allImageNames != null) {
                if (allImageNames.contains(fileName))
                {
                    System.err.println(String.format("Exist: %s", fileName));

                } else {
                    allImageNames.add(fileName);
                }
            }
            /* For debug purpose */

            File target = new File(directory + fileName);
            HttpUtility.DOWNLOAD_STATUS status = httpUtility.Download(relativeUrl, new File(directory), target);
            if (isPrint)
            {
                System.out.println(String.format("File: %s - %s", relativeUrl, status.toString()));
            }
        }
    }

    /**
     * Download all images
     * @param httpUtility
     * @param isPrint
     */
    public void DownloadAllImages(HttpUtility httpUtility, Boolean isPrint)
    {
        HashSet<String> allImageNames = new HashSet<>(); // For debug purpose
        ArrayList<DataBase> listDataBase = ReadAllMetaData(isPrint);
        int cursorList = 0;

        // Populate all items
        for (DataBase database : listDataBase) {
            int cursorDataBase = 0;
            for (Item item: database) {
                // Download ItemImage
                _downloadImage(allImageNames, item.ItemImage, IMAGE_DIR, httpUtility, isPrint);
                // Download BrandImage
                _downloadImage(null, item.Brand, BRAND_DIR, httpUtility, isPrint);
                // Download TypeImage
                _downloadImage(null, item.Type, TYPE_DIR, httpUtility, isPrint);
                // Print Procedure
                if (isPrint)
                {
                    System.out.println(String.format("DataBase: %d / %d; Items: %d / %d",
                            cursorList, listDataBase.size(),
                            cursorDataBase, database.size())
                    );
                }
                ++cursorDataBase;
            }
            ++cursorList;
        }

        System.err.println(String.format("Total Images Count: %d", allImageNames.size()));
    }
}
