package com.swws.marklang.prc_cardbook.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;

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
import java.util.Map;

public class FileUtility {

    private final String TOP_DIR = "data";
    private final String IMAGE_DIR = TOP_DIR + "/image/";
    private final String BRAND_DIR = TOP_DIR + "/brand/";
    private final String TYPE_DIR = TOP_DIR + "/type/";

    private final String META_FILE = TOP_DIR + "/meta.txt";

    private String mInternalPath;
    private Context mContext;

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
                    database = new Database(tokens[1], tokens[2]);
                } else {
                    // Use old database
                    if (line.equals("")) {
                        // Need to create new DataBase in next readLine
                        listDataBase.add(database);
                        database = null;

                    } else {
                        // Process 1 item data
                        String[] tokens = line.split(",", Item.COUNT_ELEMENT);
                        if (tokens.length != Item.COUNT_ELEMENT) {
                            throw new InvalidDataFormatException(line);
                        }

                        database.Insert(
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
                    String.format("%d,%s,%s", cursor, database.name(), database.url()),
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
     * Read a image from database
     * @param imagePathOnline
     * @return
     */
    public Bitmap ReadImage(String imagePathOnline, IMAGE_TYPE imageType) {
        // Get image path
        String imageFileName = (new File(imagePathOnline)).getName();
        // Check image file name is illegal
        if (imageFileName.lastIndexOf('.') < 0)
        {
            // Not an image file
            return null;
        }

        // TODO: Check local file
        String imagePathLocal = mInternalPath + "/";
        switch (imageType)
        {
            case IMAGE:
                imagePathLocal = imagePathLocal + IMAGE_DIR + imageFileName;
                break;

            case BRAND:
                imagePathLocal = imagePathLocal + BRAND_DIR + imageFileName;
                break;

            case TYPE:
                imagePathLocal = imagePathLocal + TYPE_DIR + imageFileName;
                break;

            default:
                Log.e(
                        getClass().getName(),
                        String.format("IMAGE TYPE %s IS ILLEGAL", imageType.toString())
                );
                return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(imagePathLocal, options);
    }

    /**
     * Download 1 image
     * @param allImageNames
     * @param relativeUrl
     * @param imageType
     * @param httpUtility
     * @param isPrint
     */
    public void DownloadImage(
            HashSet<String> allImageNames,
            String relativeUrl,
            IMAGE_TYPE imageType,
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
            String directory;
            switch (imageType)
            {
                case IMAGE:
                    directory = mInternalPath + "/" + IMAGE_DIR;
                    break;

                case BRAND:
                    directory = mInternalPath + "/" + BRAND_DIR;
                    break;

                default:
                    // TYPE
                    directory = mInternalPath + "/" + TYPE_DIR;
                    break;
            }
            httpUtility.Download(relativeUrl, directory, isPrint);
        }
    }
}
