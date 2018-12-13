package com.swws.marklang.prc_cardbook.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtility {

    private final String TOP_DIR = "data";
    private final String IMAGE_DIR = TOP_DIR + "/image/";
    private final String BRAND_DIR = TOP_DIR + "/brand/";
    private final String TYPE_DIR = TOP_DIR + "/type/";
    private final String META_FILE = TOP_DIR + "/meta.txt";
    private String internalPath;

    /**
     * Type of the image
     */
    public enum IMAGE_TYPE {
        IMAGE, BRAND, TYPE
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

    public FileUtility(Context context)
    {
        internalPath = context.getFilesDir().getPath();
    }

    private BufferedReader _getReader(String fileName)
    {
        try
        {
            FileReader fileReader = new FileReader(internalPath + "/" + fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            return bufferedReader;
        }
        catch (Exception ex) {
            return null;
        }
    }

    private String _readLine(BufferedReader reader, Boolean isPrint)
    {
        try
        {
            String line = reader.readLine();
            if (isPrint)
            {
                if (line != null)
                {
                    Log.i(this.getClass().getSimpleName(), line);
                }
            }
            return line;

        } catch (IOException ex) {

            Log.e(this.getClass().getSimpleName(), String.format("Read line failed"));
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
                    // New DataBase is reqired to be created
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
     * Read a card image
     * @param imagePathOnline
     * @return
     */
    public Bitmap ReadCardImage(String imagePathOnline, IMAGE_TYPE imageType) {
        // Get image path
        String imageFileName = (new File(imagePathOnline)).getName();
        // TODO: Check local file
        String imagePathLocal = internalPath + "/";
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
}
