package com.swws.marklang.prc_cardbook.utility;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class HttpUtility {

    /**
     * (Official) Server Error Exception
     */
    public static class ServerErrorException extends Exception {
        public String mErrorMessage;

        public ServerErrorException(String errorMessage)
        {
            super();
            mErrorMessage = errorMessage;
        }
    }

    /**
     * Directory Creation Error Exception
     */
    public static class DirCreateException extends Exception {
        public String mDirectory;

        public DirCreateException(String directory)
        {
            super();
            mDirectory = directory;
        }
    }

    /**
     * Get HTML Content String from URL
     * @param URLString
     * @return contents; null once Error occurs
     */
    public String GetHtmlContent(String URLString) throws ServerErrorException, IOException
    {
        try {
            URL URLObject = new URL(URLString);
            HttpURLConnection connection = (HttpURLConnection)URLObject.openConnection();
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200)
            {
                // Read contents
                StringBuilder sb = new StringBuilder();
                InputStream inputStream = connection.getInputStream();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                    sb.append('\n');
                }
                in.close();

                return sb.toString();

            } else {
                // Server Error
                String errorMessage = String.format(
                        Locale.JAPAN,
                        "URL: %s\n Sever Error with RC: %d",
                        URLString,
                        responseCode
                );
                Log.e(this.getClass().getSimpleName(), errorMessage);
                throw new ServerErrorException(errorMessage);
            }

        } catch (MalformedURLException ex) {
            // URL format error
            Log.e(this.getClass().getSimpleName(), "URL Parse Failed.");
            throw ex;

        } catch (IOException ex) {
            // Network error
            Log.e(this.getClass().getSimpleName(), "Connection Failed.");
            throw ex;
        }
    }

    /**
     * Download file from given URL to given destination
     * @param urlPrefix
     * @param relativeUrl
     * @param directory
     * @param isPrint
     * @throws DirCreateException
     * @throws IOException
     */
    public void Download(
            String urlPrefix, String relativeUrl, String directory, boolean isPrint)
            throws DirCreateException, IOException
    {
        // Generate the absolute URL by concatenation
        String URLLink = urlPrefix + relativeUrl;

        try
        {
            // Check the existence of the directory TODO: Can create the directory iteratively?
            File dirFile = new File(directory);
            if (!dirFile.exists()) {
                // Directory does NOT exist
                Log.w(this.getClass().getSimpleName(),
                        String.format("Directory %s does not exist.", dirFile.getName()));

                // Create a new directory
                if (dirFile.mkdir()) {
                    Log.w(this.getClass().getSimpleName(),
                            String.format("Directory %s created.", dirFile.getName()));

                } else {
                    Log.e(this.getClass().getSimpleName(),
                            String.format("Directory %s cannot be created.", dirFile.getName()));
                    throw new DirCreateException(dirFile.getName());
                }
            }

            // Check the existence of the target file
            String fileName = (new File(relativeUrl)).getName();
            File target = new File(directory + fileName);
            if (target.exists()) {
                // File existed - Skip
                if (isPrint) {
                    Log.i(this.getClass().getSimpleName(),
                            String.format("File: %s - EXIST", fileName));
                }
                return;
            }

            // Download
            URL URLObject = new URL(URLLink);
            FileUtils.copyURLToFile(URLObject, target);
            // File is downloaded
            if (isPrint) {
                Log.i(this.getClass().getSimpleName(), String.format("File: %s - OK", fileName));
            }

        } catch (MalformedURLException ex) {
            Log.e(this.getClass().getSimpleName(), String.format("URL Parse Failed: %s", URLLink));
            throw ex;

        } catch (IOException ex) {
            Log.e(this.getClass().getSimpleName(), String.format("Connection Failed: %s", URLLink));
            throw ex;
        }
    }
}
