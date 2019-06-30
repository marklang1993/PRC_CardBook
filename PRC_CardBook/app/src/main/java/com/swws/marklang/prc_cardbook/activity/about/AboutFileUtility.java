package com.swws.marklang.prc_cardbook.activity.about;

import com.swws.marklang.prc_cardbook.utility.AssetsFileUtility;

import java.io.BufferedReader;

public abstract class AboutFileUtility extends AssetsFileUtility {

    // Constants
    private static final boolean ABOUT_DEBUG_IS_PRINT = false;

    /**
     * Read out the license contents
     * @param licenseTextFileName
     * @return
     */
    protected String readLicenseContent(String licenseTextFileName) {
        // Get reader
        BufferedReader reader = getReader(licenseTextFileName);
        if (reader == null) return null;

        // Read the content
        StringBuilder content = new StringBuilder();
        while (true) {
            String rawLine = readLine(reader, ABOUT_DEBUG_IS_PRINT);
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
