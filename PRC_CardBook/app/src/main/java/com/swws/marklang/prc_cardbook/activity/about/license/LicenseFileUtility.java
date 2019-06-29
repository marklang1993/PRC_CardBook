package com.swws.marklang.prc_cardbook.activity.about.license;

import com.swws.marklang.prc_cardbook.utility.AssetsFileUtility;

import java.io.BufferedReader;
import java.util.ArrayList;

public class LicenseFileUtility extends AssetsFileUtility {

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

    /**
     * Read out the license contents
     * @param licenseTextFileName
     * @return
     */
    private String readLicenseContent(String licenseTextFileName) {
        // Get reader
        BufferedReader reader = getReader(licenseTextFileName);
        if (reader == null) return null;

        // Read the content
        StringBuilder content = new StringBuilder();
        while (true) {
            String rawLine = readLine(reader, LICENSE_DEBUG_IS_PRINT);
            if (rawLine == null) break;
            content.append(removeLeadingSpaces(rawLine));
            content.append("\n");
        }

        if (content.length() == 0) return null;
        return removeRedundantNewline(content);
    }

    /**
     * Remove the leading spaces of the given raw string
     * @param rawLine
     * @return
     */
    private String removeLeadingSpaces(String rawLine) {
        int rawLineLength = rawLine.length();
        StringBuilder sb = new StringBuilder(rawLineLength);
        boolean isTextStart = false;

        for(int i = 0; i < rawLineLength; ++i) {
            if (isTextStart) {
                // Copy the rest characters to the target StringBuilder
                sb.append(rawLine.charAt(i));

            } else {
                if (rawLine.charAt(i) == ' ') {
                    // This is a leading space
                    ;

                } else {
                    // Start copy the characters from here
                    sb.append(rawLine.charAt(i));
                    isTextStart = true;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Remove redundant newline within a paragraph
     * TODO: this could be a coding interview question
     * @param content
     * @return
     */
    private String removeRedundantNewline(StringBuilder content) {
        int contentLength = content.length();
        if (contentLength < 2) return content.toString();
        StringBuilder sb = new StringBuilder(contentLength);

        // This can be considered as a state machine
        boolean isNewState = false;
        for (int i = 0; i < contentLength; ++i) {
            char curChar = content.charAt(i);

            if (isNewState) {
                // Last character is newline
                if (curChar == '\n') {
                    // Output 2 newlines and jump back to the original state
                    sb.append('\n');
                    sb.append('\n');
                    isNewState = false;

                } else {
                    // Output nothing, just jump back to the original state
                    isNewState = false;
                }

            } else {
                // Last character is not newline
                if (curChar == '\n') {
                    // Output nothing, just change the state
                    isNewState = true;

                } else {
                    // Output current character
                    sb.append(curChar);
                    isNewState = false;
                }
            }
        }
        return sb.toString();
    }
}
