package com.swws.marklang.prc_cardbook.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

public class FileUtility {

    private final String TOP_DIR = "data";
    private final String IMAGE_DIR = TOP_DIR + "/image/";
    private final String BRAND_DIR = TOP_DIR + "/brand/";
    private final String TYPE_DIR = TOP_DIR + "/type/";

    private final String META_FILE = TOP_DIR + "/meta.txt";

    private String mInternalPath;
    private Context mContext;

    // Constants
    private final int LENGTH_DATABASE_LINE_HEADER = 4;

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
     * @param context
     */
    public FileUtility(Context context)
    {
        mContext = context;
        mInternalPath = mContext.getFilesDir().getPath();

        // Check the existence of TOP_DIR
        File topDir = new File(mInternalPath + "/" + TOP_DIR);
        if (!topDir.exists()) {
            // Create TOP_DIR
            topDir.mkdir();
        }
    }

    /**
     * Get BufferedReader (For text file)
     * @param fileName relative path w.r.t "internalPath"
     * @return
     */
    private BufferedReader _getReader(String fileName)
    {
        try
        {
            FileReader fileReader = new FileReader(mInternalPath + "/" + fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            return bufferedReader;
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * Read one line text from BufferedReader
     * @param reader
     * @param isPrint Output to Log.i() ?
     * @return
     */
    private String _readLine(BufferedReader reader, Boolean isPrint)
    {
        try
        {
            String line = reader.readLine();
            if (isPrint)
            {
                if (line != null)
                {
                    Log.i(this.getClass().getSimpleName(), "Reader: " + line);
                }
            }
            return line;

        } catch (IOException ex) {

            Log.e(this.getClass().getSimpleName(), String.format("Read line failed"));
            return null;
        }
    }

    /**
     * Get BufferedWriter (For text file)
     * @param fileName relative path w.r.t "internalPath"
     * @return
     */
    private BufferedWriter _getWriter(String fileName)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(mInternalPath + "/" + fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            return bufferedWriter;
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * Read one line text from BufferedReader
     * @param writer
     * @param line One line text
     * @param isPrint Output to Log.i() ?
     * @return
     */
    private void _writeLine(BufferedWriter writer, String line, Boolean isPrint)
    {
        try
        {
            writer.write(line, 0, line.length());
            writer.write('\n');
            if (isPrint)
            {
                Log.i(this.getClass().getSimpleName(), "Writer: " + line);
            }

        } catch (IOException ex)
        {
            Log.e(this.getClass().getSimpleName(), String.format("Write line failed: %s", line));
        }
    }

    /**
     * Close a BufferedReader or BufferedWriter
     * @param operator
     */
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
     * Check is metadata file presented
     * @return
     */
    public boolean IsMetadataFilePresented() {
        BufferedReader bufferedReader = _getReader(META_FILE);

        if (bufferedReader == null) {
            // Metadata file is not presented OR not accessible.
            return false;
        }

        _close(bufferedReader);
        return true;
    }

    /**
     * Read all meta data from META_FILE
     * @param isPrint
     * @return
     */
    public ArrayList<Database> ReadAllMetaData(Boolean isPrint)
    {
        BufferedReader bufferedReader = _getReader(META_FILE);
        ArrayList<Database> listDataBase = new ArrayList<>();

        // Read data
        try
        {
            Database database = null;
            String line = _readLine(bufferedReader, isPrint);
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
                line = _readLine(bufferedReader, isPrint);
            }

        } catch (InvalidDataFormatException ex) {
            Log.e(this.getClass().getSimpleName(), String.format("Invalid Data Format: %s", ex.InvalidData));

        } finally {
            _close(bufferedReader);
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
        BufferedWriter bufferedWriter = _getWriter(META_FILE);
        Iterator<Database> iterator = databases.iterator();
        int cursor = 0;
        while (iterator.hasNext())
        {
            // Get current database
            Database database = iterator.next();
            ++cursor;

            // Write the name and url of the database
            _writeLine(bufferedWriter,
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
                _writeLine(bufferedWriter,
                        item.toString(),
                        isPrint
                );
            }

            // Write one more line as separator
            _writeLine(bufferedWriter,
                    "",
                    isPrint
            );
        }

        // Close the writer
        _close(bufferedWriter);
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
                directory = mInternalPath + "/" + IMAGE_DIR + seasonDir + "/";
                break;

            case BRAND:
                directory = mInternalPath + "/" + BRAND_DIR + seasonDir + "/";
                break;

            default:
                // TYPE
                directory = mInternalPath + "/" + TYPE_DIR + seasonDir + "/";
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
     * @param allImageNames
     * @param relativeUrl
     * @param imageType
     * @param seasonID
     * @param httpUtility
     * @param isPrint
     */
    public void DownloadImage(
            HashSet<String> allImageNames,
            String relativeUrl,
            IMAGE_TYPE imageType,
            SeasonID seasonID,
            HttpUtility httpUtility,
            Boolean isPrint
    )
            throws HttpUtility.DirCreateException, IOException
    {
        // Validate relative Url
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

            /* For debug purpose */
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
            httpUtility.Download(relativeUrl, path, isPrint);
        }
    }
}
