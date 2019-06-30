package com.swws.marklang.prc_cardbook.activity.about.license;

import com.swws.marklang.prc_cardbook.activity.about.AboutFileUtility;

import java.io.BufferedReader;
import java.util.ArrayList;

public class LicenseFileUtility extends AboutFileUtility {

    // Singleton
    private static LicenseFileUtility mLicenseFileUtility = null;

    // Constants
    private static final String ROOT_DIR = "lib_license";
    private static final String INDEX_FILE = "index.list";
    private static final int LICENSE_LIST_TOKEN_COUNT = 4;
    private static final boolean LICENSE_DEBUG_IS_PRINT = false;

    /**
     * Constructor
     */
    private LicenseFileUtility() {
        super();
    }

    /**
     * Get an instance of LicenseFileUtility
     * @return
     */
    public static LicenseFileUtility getInstance() {
        // TODO: thread-safe?
        if (mLicenseFileUtility == null) {
            mLicenseFileUtility = new LicenseFileUtility();
        }
        return mLicenseFileUtility;
    }

    @Override
    protected String initRootDirectory() {
        return ROOT_DIR;
    }

    /**
     * Get license list
     * @return null once error occurs
     */
    public ArrayList<LicenseItem> getList() {
        // Get reader
        BufferedReader reader = getReader(INDEX_FILE);
        if (reader == null) return null;

        // Skip the first line
        readLine(reader, LICENSE_DEBUG_IS_PRINT);

        // Read the list
        ArrayList<LicenseItem> licenseItems = new ArrayList<>();
        while (true) {
            String rawLine = readLine(reader, LICENSE_DEBUG_IS_PRINT);
            if (rawLine == null) break;

            // Check the format
            String[] tokens = rawLine.split(",");
            if (tokens.length != LICENSE_LIST_TOKEN_COUNT) {
                // Invalid format
                licenseItems.clear();
                break;
            }

            // Construct an LicenseItem
            LicenseItem licenseItem = new LicenseItem();
            licenseItem.LicenseTextFile = tokens[0];
            licenseItem.LibraryName = tokens[1];
            licenseItem.CopyrightHolder = tokens[2];
            licenseItem.RepositoryLink = tokens[3];
            licenseItem.IsShrinkLicenseContentDisplay = false;

            licenseItems.add(licenseItem);
        }
        close(reader);

        // Check licenseItems is empty
        if (licenseItems.size() == 0) return null;

        // Get LicenseContent
        for (int i = 0; i < licenseItems.size(); ++i) {
            LicenseItem licenseItem = licenseItems.get(i);
            licenseItem.LicenseContent = readLicenseContent(licenseItem.LicenseTextFile);;
        }

        return licenseItems;
    }
}
