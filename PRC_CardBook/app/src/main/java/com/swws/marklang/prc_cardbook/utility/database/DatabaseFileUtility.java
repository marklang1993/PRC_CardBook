package com.swws.marklang.prc_cardbook.utility.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.HttpUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

public final class DatabaseFileUtility extends FileUtility{

    // Singleton instance
    private static DatabaseFileUtility mDatabaseFileUtility = null;

    // Constants
    private static final String TOP_DIR = "data";
    private static final String IMAGE_DIR = TOP_DIR + "/image/";
    private static final String BRAND_DIR = TOP_DIR + "/brand/";
    private static final String TYPE_DIR = TOP_DIR + "/type/";
    private static final String META_FILE = TOP_DIR + "/meta.txt";

    private static final int LENGTH_DATABASE_LINE_HEADER = 4;

    /**
     * Type of the image
     */
    public enum IMAGE_TYPE {
        IMAGE,
        BRAND,
        TYPE
    }

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

    /**
     * Constructor
     */
    private DatabaseFileUtility()
    {
        // Check the existence of TOP_DIR
        // TODO: check the return value
        checkAndMakeDirectory(TOP_DIR);
    }

    /**
     * Get an instance of DatabaseFileUtility
     * @return
     */
    public static DatabaseFileUtility getInstance()
    {
        // TODO: thread-safe?
        if (mDatabaseFileUtility == null) {
            mDatabaseFileUtility = new DatabaseFileUtility();
        }
        return mDatabaseFileUtility;
    }

    /**
     * Check is metadata file presented
     * @return
     */
    public boolean IsMetadataFilePresented() {
        return isFilePresented(META_FILE);
    }

    /**
     * Read all meta data from META_FILE
     * @param isPrint
     * @return
     */
    public ArrayList<Database> ReadAllMetaData(Boolean isPrint)
    {
        BufferedReader bufferedReader = getReader(META_FILE);
        ArrayList<Database> listDataBase = new ArrayList<>();

        // Read data
        try
        {
            Database database = null;
            String line = readLine(bufferedReader, isPrint);
            while(line != null) {
                // Process
                if (database == null) {
                    // New DataBase is required to be created
                    String[] tokens = line.split(",");

                    // Read the header of Database line
                    if (tokens.length == LENGTH_DATABASE_LINE_HEADER) {
                        // Create a new Database
                        database = new Database(tokens[1], tokens[2], SeasonID.valueOf(tokens[3]));

                    } else {
                        // Wrong format
                        throw new InvalidDataFormatException(line);
                    }

                } else {
                    // Use old database
                    if (line.equals("")) {
                        // Need to create new DataBase in next readLine
                        listDataBase.add(database);
                        database = null;

                    } else {
                        // Process 1 item data and add it to the current database
                        database.Insert(line);
                    }
                }
                // Read Next Line
                line = readLine(bufferedReader, isPrint);
            }

        } catch (InvalidDataFormatException ex) {
            Log.e(this.getClass().getSimpleName(), String.format("Invalid Data Format: %s", ex.InvalidData));

        } finally {
            close(bufferedReader);
        }

        return listDataBase;
    }

    /**
     * Write new meta data
     * @param databases
     * @param isPrint
     */
    public void WriteAllMetaData(LinkedList<Database> databases, Boolean isPrint)
    {
        BufferedWriter bufferedWriter = getWriter(META_FILE);
        Iterator<Database> iterator = databases.iterator();
        int cursor = 0;
        while (iterator.hasNext())
        {
            // Get current database
            Database database = iterator.next();
            ++cursor;

            // Write the name and url of the database
            writeLine(bufferedWriter,
                    String.format(Locale.JAPAN, "%d,%s,%s,%s",
                            cursor,
                            database.name(),
                            database.url(),
                            database.seasonId().toString()),
                    isPrint
            );

            // Write all items
            for (Item item:
                    database) {
                writeLine(bufferedWriter,
                        item.toString(),
                        isPrint
                );
            }

            // Write one more line as separator
            writeLine(bufferedWriter,
                    "",
                    isPrint
            );
        }

        // Close the writer
        close(bufferedWriter);
    }


    /**
     * Get image path from its type
     * @param imageType
     * @return
     */
    private String getImagePath(IMAGE_TYPE imageType, SeasonID seasonID) {
        String directory;
        String seasonDir = "" + seasonID.ordinal();

        switch (imageType)
        {
            case IMAGE:
                directory = getInternalPath() + "/" + IMAGE_DIR + seasonDir + "/";
                break;

            case BRAND:
                directory = getInternalPath() + "/" + BRAND_DIR + seasonDir + "/";
                break;

            default:
                // TYPE
                directory = getInternalPath() + "/" + TYPE_DIR + seasonDir + "/";
                break;
        }

        return directory;
    }

    /**
     * Read a image from database
     * @param imagePathOnline
     * @param imageType
     * @param seasonID
     * @return
     */
    public Bitmap ReadImage(String imagePathOnline, IMAGE_TYPE imageType, SeasonID seasonID) {
        // Get image path
        String imageFileName = (new File(imagePathOnline)).getName();
        // Check image file name is illegal
        if (imageFileName.lastIndexOf('.') < 0)
        {
            // Not an image file
            return null;
        }

        // Read image file
        String imagePath = getImagePath(imageType, seasonID) + imageFileName;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    /**
     * Download 1 image
     * @param allImageNames A Hashset that is used to save all image names for DEBUG
     * @param urlPrefix
     * @param relativeUrl
     * @param imageType
     * @param seasonID
     * @param httpUtility
     * @param isPrint
     */
    public void DownloadImage(
            HashSet<String> allImageNames,
            String urlPrefix,
            String relativeUrl,
            IMAGE_TYPE imageType,
            SeasonID seasonID,
            HttpUtility httpUtility,
            Boolean isPrint
    )
            throws HttpUtility.DirCreateException, IOException
    {
        // Validate relative Url
        if (relativeUrl == null) {
            return;
        }

        if (!relativeUrl.equals(""))
        {
            // "relativeUrl" must be NOT empty
            String fileName = (new File(relativeUrl)).getName();

            // Check is the given file name an image file
            if (!fileName.contains(".")) {
                // This is not an image file - no extension
                Log.e(this.getClass().getSimpleName(), String.format("Not an Image File: %s", fileName));
                return;
            }

            /* For DEBUG purpose */
            if (allImageNames != null) {
                if (allImageNames.contains(fileName))
                {
                    Log.e(this.getClass().getSimpleName(), String.format("Exist: %s", fileName));

                } else {
                    allImageNames.add(fileName);
                }
            }

            // Download the image
            String path = getImagePath(imageType, seasonID);
            httpUtility.Download(urlPrefix, relativeUrl, path, isPrint);
        }
    }
}
