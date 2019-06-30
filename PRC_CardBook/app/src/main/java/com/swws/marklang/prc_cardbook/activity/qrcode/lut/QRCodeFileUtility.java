package com.swws.marklang.prc_cardbook.activity.qrcode.lut;

import android.util.Log;

import com.swws.marklang.prc_cardbook.utility.AssetsFileUtility;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;

public class QRCodeFileUtility extends AssetsFileUtility {

    // Singleton
    private static QRCodeFileUtility mQRCodeFileUtility = null;

    // Constants
    private static final String ROOT_DIR = "qrcode_lut";
    private static final String INDEX_FILE = "index.list";
    private static final int QRCODE_LUT_TOKEN_COUNT = 6;
    private static final boolean QRCODE_DEBUG_IS_PRINT = false;

    // Internal variables
    private HashMap<String, QRCodeLUTItem> mQRCodeLUT;

    /**
     * Constructor
     */
    private QRCodeFileUtility() {
        super();

        // Init. QRCodeLUT
        LinkedList<String> fileList = getLUTFileList();
        mQRCodeLUT = initQRCodeLUT(fileList);
    }

    /**
     * Get an instance of QRCodeFileUtility
     * @return
     */
    public static QRCodeFileUtility getInstance() {
        // TODO: thread-safe? IMPORTANT!
        if (mQRCodeFileUtility == null) {
            mQRCodeFileUtility = new QRCodeFileUtility();
        }
        return mQRCodeFileUtility;
    }

    @Override
    protected String initRootDirectory() {
        return ROOT_DIR;
    }

    /**
     * Get LUT item
     * @param digest
     * @return
     */
    public QRCodeLUTItem getLUTItem(String digest) {
        if (digest == null) return null;

        if (mQRCodeLUT.containsKey(digest)) {
            return mQRCodeLUT.get(digest);
        }
        // Not found
        return null;
    }

    /**
     * Initialize QRCodeLUT
     * @param fileList
     * @return
     */
    private HashMap<String, QRCodeLUTItem> initQRCodeLUT(LinkedList<String> fileList) {
        HashMap<String, QRCodeLUTItem> lut = new HashMap<>();
        if (fileList == null) return lut;

        // Read each lut file
        for (String fileName: fileList) {
            readLUTFile(fileName, lut);
        }

        return lut;
    }

    /**
     * Read a single lut file
     * @param fileName
     * @param lut
     */
    private void readLUTFile(String fileName, HashMap<String, QRCodeLUTItem> lut) {
        // Get reader
        BufferedReader reader = getReader(fileName);
        if (reader == null) return;

        // Skip the first line
        readLine(reader, QRCODE_DEBUG_IS_PRINT);

        // Read out LUT file
        while (true) {
            // Read one item
            String rawLine = readLine(reader, QRCODE_DEBUG_IS_PRINT);
            if (rawLine == null) break; // reached EOF

            // Get tokens
            String[] tokens = rawLine.split(",");
            if (tokens.length != QRCODE_LUT_TOKEN_COUNT) {
                // Wrong format - skip this LUT file
                Log.e(this.getClass().getSimpleName(), "Wrong Format: " + rawLine);
                break;
            }

            // Construct a LUT item
            QRCodeLUTItem lutItem = new QRCodeLUTItem();
            lutItem.SeasonID = tokens[1];
            lutItem.ImageID = tokens[2];
            lutItem.JRColor = tokens[3];

            // Insert to the LUT
            String mainKey = tokens[0];
            lut.put(mainKey, lutItem);
        }
        close(reader);
    }

    /**
     * Get LUT describe file list
     * @return null once nothing is read out OR error occurred
     */
    private LinkedList<String> getLUTFileList() {
        // Get reader
        BufferedReader reader = getReader(INDEX_FILE);
        if (reader == null) return null;

        // Read out LUT file list
        LinkedList<String> fileList = new LinkedList<>();
        while (true) {
            // Read one item
            String fileName = readLine(reader, QRCODE_DEBUG_IS_PRINT);
            if (fileName == null) break;
            fileList.add(fileName);
        }
        close(reader);

        // Validate
        if (fileList.size() == 0) return null;

        return fileList;
    }


}
