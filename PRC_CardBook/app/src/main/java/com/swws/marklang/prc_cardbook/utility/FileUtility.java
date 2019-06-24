package com.swws.marklang.prc_cardbook.utility;

import android.util.Log;

import com.swws.marklang.prc_cardbook.activity.main.MainLoadActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtility {

    // Internal storage path
    private String mInternalPath;

    /**
     * Constructor
     */
    protected FileUtility() {
        // Get internal storage path from Context
        mInternalPath = MainLoadActivity.getCurrentApplicationContext().getFilesDir().getPath();
    }

    /**
     * Get BufferedReader (For text file)
     * @param fileName relative path w.r.t "internalPath"
     * @return
     */
    protected BufferedReader getReader(String fileName)
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
    protected String readLine(BufferedReader reader, Boolean isPrint)
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
    protected BufferedWriter getWriter(String fileName)
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
    protected void writeLine(BufferedWriter writer, String line, Boolean isPrint)
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
    protected void close(Closeable operator)
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
     * Check a directory does exist, otherwise create this directory
     * @param dirName
     * @return
     */
    protected boolean checkAndMakeDirectory(String dirName) {
        // Construct the full path of the given directory
        File dir = new File(mInternalPath + "/" + dirName);
        if (!dir.exists()) {
            // Create the corresponding directory
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * Check the file with given path is presented or not
     * @param filePath
     * @return
     */
    protected boolean isFilePresented(String filePath) {
        File file = new File(mInternalPath + "/" + filePath);
        return file.exists();
    }

    /**
     * Get the path of internal storage
     * @return
     */
    protected String getInternalPath() {
        return mInternalPath;
    }
}
