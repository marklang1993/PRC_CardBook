package com.swws.marklang.prc_cardbook.activity.about;

import android.util.Log;

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
            content.append(removeRedundantSpaces(rawLine));
            content.append("\n");
        }

        if (content.length() == 0) return null;
        return removeRedundantNewline(content);
    }

    /**
     * Remove leading and trailing spaces of the given raw string
     * @param rawLine
     * @return
     */
    private String removeRedundantSpaces(String rawLine) {
        int rawLineLength = rawLine.length();
        int startPos, endPos;

        // Check is this line empty
        if (rawLineLength == 0) return rawLine;

        // Find the real start position of the string
        for(startPos = 0; startPos < rawLineLength; ++startPos) {
            if (rawLine.charAt(startPos) != ' ') break;
        }

        // Check whether this line contains only spaces
        if (startPos == rawLineLength) return (String) "";

        // Find the real end position of the string
        for (endPos = rawLineLength - 1; endPos >= 0; --endPos) {
            if (rawLine.charAt(endPos) != ' ') {
                ++endPos;
                break;
            }
        }

        return rawLine.substring(startPos, endPos);
    }

    /**
     * Remove redundant newline within a paragraph
     * @param content
     * @return
     */
    private String removeRedundantNewline(StringBuilder content) {
        int contentLength = content.length();
        if (contentLength < 2) return content.toString();
        StringBuilder sb = new StringBuilder(contentLength);

        // This can be considered as a state machine
        boolean isLastCharNewLineState = false;
        for (int i = 0; i < contentLength; ++i) {
            char curChar = content.charAt(i);

            if (isLastCharNewLineState) {
                // Last character is newline
                if (curChar == '\n') {
                    // Output 2 newlines and jump back to the original state
                    sb.append('\n');
                    sb.append('\n');
                    isLastCharNewLineState = false;

                } else {
                    // Output current character and jump back to the original state
                    sb.append(curChar);
                    isLastCharNewLineState = false;
                }

            } else {
                // Last character is not newline
                if (curChar == '\n') {
                    // Output a space and change the state
                    sb.append(' ');
                    isLastCharNewLineState = true;

                } else {
                    // Output current character
                    sb.append(curChar);
                    isLastCharNewLineState = false;
                }
            }
        }
        return sb.toString();
    }
}
