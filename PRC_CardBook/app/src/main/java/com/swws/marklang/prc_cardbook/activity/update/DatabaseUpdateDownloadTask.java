package com.swws.marklang.prc_cardbook.activity.update;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.HttpUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class DatabaseUpdateDownloadTask extends AsyncTask<Void, String, Boolean> {

    private DatabaseUpdateActivity mParentActivity;
    private Context mContext;
    private Button mDatabaseUpdateStartButton;
    private ProgressBar mDatabaseUpdateProgressBar;
    private TextView mDatabaseUpdateStatusTextView;

    private int mStartOption; // 0: Start by user, 1: Start by app (in the first launching).

    private HttpUtility mHttpUtility;
    private FileUtility mFileUtility;
    /**
     * Index: Message
     * 0    : info_download_pages_list
     * 1    : info_download_coordinates
     * 2    : info_download_coordinate_images
     * 3    : info_download_write_meta_file
     * 4:   : info_download_complete
     */
    private final int[] mProgressValues = {0, 5, 40, 99, 100};

    // For debug purpose
    private final boolean mIsPrintDebug = true;

    public DatabaseUpdateDownloadTask(
            DatabaseUpdateActivity parentActivity,
            Button databaseUpdateStartButton,
            ProgressBar databaseUpdateProgressBar,
            TextView databaseUpdateStatusTextView,
            int startOption
    ) {
        // Get parent activity
        mParentActivity = parentActivity;
        // Get Context
        mContext = mParentActivity.getApplicationContext();
        // Get StartOption
        mStartOption = startOption;

        // Init.
        mHttpUtility = new HttpUtility(mContext);
        mFileUtility = new FileUtility(mContext);

        // Update references of UI components
        mDatabaseUpdateStartButton = databaseUpdateStartButton;
        mDatabaseUpdateProgressBar = databaseUpdateProgressBar;
        mDatabaseUpdateStatusTextView = databaseUpdateStatusTextView;
    }

    /**
     * Downloading task is executed here
     * @param voids
     * @return
     */
    @Override
    protected Boolean doInBackground(Void... voids) {

        ArrayList<Database> oldDatabases = MainActivity.getAllDatabases();
        try {
            // 1. Get the directory of all urls
            LinkedHashMap<String, String> urlDict = getUrlDict(oldDatabases);
            // Check is up-to-date
            if (urlDict.size() != 0)
            {
                // 2. Get databases of all series
                LinkedList<Database> databases = getDatabaseLinkedList(urlDict);

                // 3. Get new item images
                getItemImages(databases);

                // 4. Write new metadata
                writeNewMetaData(oldDatabases, databases);
            }

            // Show the final progress status
            publishProgress(getProgressMsg(mProgressValues[4],
                    mContext.getString(R.string.info_download_complete))
            );

            // Delay for a while
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) { }

        } catch (HttpUtility.ServerErrorException e) {
            // Server Error
            publishProgress(getProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_official_server_malfunction))
            );
            return false;

        } catch (HttpUtility.DirCreateException e) {
            // Cannot Create Direction
            publishProgress(getProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_directory_creation_failed))
            );
            return false;

        } catch (MalformedURLException e) {
            // URL is Incorrect
            publishProgress(getProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_app_update_required))
            );
            return false;

        } catch (IOException e) {
            // Connection Failed
            publishProgress(getProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_no_internet))
            );
            return false;
        }

        return true;
    }

    /**
     * 1st Step: Get the dictionary of all URLs
     * @return
     * @throws HttpUtility.ServerErrorException
     * @throws IOException
     */
    private LinkedHashMap<String, String> getUrlDict(ArrayList<Database> oldDatabases)
            throws HttpUtility.ServerErrorException, IOException {

        // Update the initial value of the progress bar
        publishProgress(getProgressMsg(mProgressValues[0],
                mContext.getString(R.string.info_download_pages_list))
        );

        // Retrieve the raw dictionary from the official website
        LinkedHashMap<String, String> rawUrlDict = mHttpUtility.GetUrlDict(mIsPrintDebug);

        // Generate a set of existed keys
        HashSet<String> existedKeys = new HashSet<>();
        if (oldDatabases != null) {
            // If the oldDatabases is presented
            for (Database oldDatabase: oldDatabases) {
                existedKeys.add(oldDatabase.url());
            }
        }

        // Generate the final dictionary by removing the existed series that are in the local database
        LinkedHashMap<String, String> urlDict = new LinkedHashMap<>();
        int countEntries = rawUrlDict.size();
        Iterator<Map.Entry<String, String>> iterator = rawUrlDict.entrySet().iterator();
        for (int i = 0; i < countEntries; ++i)
        {
            Map.Entry<String, String> rawUrlEntry = iterator.next();
            if (!existedKeys.contains(rawUrlEntry.getKey())) {
                // A new series is found
                urlDict.put(rawUrlEntry.getKey(), rawUrlEntry.getValue());
            }
        }

        return urlDict;
    }

    /**
     * 2nd Step: Get all databases based on "urlDict"
     * @param urlDict
     * @return
     * @throws HttpUtility.ServerErrorException
     * @throws IOException
     */
    private LinkedList<Database> getDatabaseLinkedList(LinkedHashMap<String, String> urlDict)
            throws HttpUtility.ServerErrorException, IOException {

        // Update the initial value of the progress bar
        publishProgress(getProgressMsg(mProgressValues[1],
                mContext.getString(R.string.info_download_coordinates))
        );

        // Retrieve each database from website based on "urlDict"
        LinkedList<Database> databases = new LinkedList<>();
        int countDatabases = urlDict.size();
        Iterator<Map.Entry<String, String>> iterator = urlDict.entrySet().iterator();
        for (int i = 0; i < countDatabases; ++i)
        {
            Map.Entry<String, String> urlEntry = iterator.next();

            // Update the value of the progress bar
            int curretProgress = calculateCurrentProgressValue(
                    i, 0, countDatabases, mProgressValues[1], mProgressValues[2] - 1
            );
            publishProgress(getProgressMsg(curretProgress,
                    mContext.getString(R.string.info_download_coordinates) + " - " + urlEntry.getValue()
                    )
            );

            // Get the database along with its all items
            Database dataBase = mHttpUtility.PopulateAllItems(
                    urlEntry.getValue(),
                    urlEntry.getKey(),
                    mIsPrintDebug
            );

            // Add this Database
            databases.add(dataBase);
        }
        return databases;
    }

    /**
     * 3rd Step: Get all item images
     * @param databases
     */
    private void getItemImages(LinkedList<Database> databases)
            throws HttpUtility.DirCreateException, IOException
    {
        HashSet<String> allImageNames = new HashSet<>(); // For debug purpose

        // Update the initial value of the progress bar
        publishProgress(getProgressMsg(mProgressValues[2],
                mContext.getString(R.string.info_download_coordinate_images))
        );

        // Calculate the total count of images
        int totalCount = 0;
        for (Database database : databases) {
            totalCount += database.size();
        }

        // Populate all databases
        int cursorItem = 0;
        int cursorList = 0;
        for (Database database : databases) {
            // Populate all items
            int cursorDataBase = 0;
            for (Item item: database) {

                // Update the value of the progress bar
                int currentProgress = calculateCurrentProgressValue(
                        cursorItem, 0, totalCount, mProgressValues[2], mProgressValues[3] - 1
                );
                publishProgress(getProgressMsg(currentProgress,
                        mContext.getString(R.string.info_download_coordinate_images) + "\n" + item.ItemName
                        )
                );

                // Download ItemImage
                mFileUtility.DownloadImage(
                        allImageNames, item.ItemImage, FileUtility.IMAGE_TYPE.IMAGE, mHttpUtility, mIsPrintDebug);
                // Download BrandImage
                mFileUtility.DownloadImage(
                        null, item.Brand, FileUtility.IMAGE_TYPE.BRAND, mHttpUtility, mIsPrintDebug);
                // Download TypeImage
                mFileUtility.DownloadImage(
                        null, item.Type, FileUtility.IMAGE_TYPE.TYPE, mHttpUtility, mIsPrintDebug);

                // Print Procedure
                if (mIsPrintDebug)
                {
                    Log.i(this.getClass().getSimpleName(),
                            String.format("DataBase: %d / %d; Items: %d / %d",
                                    cursorList + 1, databases.size(),
                                    cursorDataBase + 1, database.size())
                    );
                }
                ++cursorItem;
                ++cursorDataBase;
            }
            ++cursorList;
        }

        Log.e(this.getClass().getSimpleName(), String.format("Total Images Count: %d", allImageNames.size()));
    }

    /**
     * 4th Step: Write new metadata
     * @param oldDatabases
     * @param newDatabases
     */
    private void writeNewMetaData(ArrayList<Database> oldDatabases, LinkedList<Database> newDatabases)
    {
        // Update the initial value of the progress bar
        publishProgress(getProgressMsg(mProgressValues[3],
                mContext.getString(R.string.info_download_write_meta_file))
        );

        // Move all databases from "oldDatabases" to newDatabases
        for (Database oldDatabase: oldDatabases) {
            newDatabases.add(oldDatabase);
        }

        // Go to write metadata
        mFileUtility.WriteAllMetaData(newDatabases, mIsPrintDebug);
    }

    /**
     * Calculate the value of current progress
     * @param currentActual current value of actual value
     * @param minActual minimal value of actual value
     * @param maxActual maximal value of actual value
     * @param minProgress minimal value of the value used by ProgressBar
     * @param maxProgress maximal value of the value used by ProgressBar
     * @return
     */
    private int calculateCurrentProgressValue(
            int currentActual,
            int minActual,
            int maxActual,
            int minProgress,
            int maxProgress
    ) {
        int rangeActual = maxActual - minActual;
        int deltaActual = currentActual - minActual;
        int rangeProgress = maxProgress - minProgress;

        double percentageActual = (double)deltaActual / (double)rangeActual;

        return (int)(rangeProgress * percentageActual) + minProgress;
    }

    /**
     * Construct a Progress Message Array
     * @param percentage if percentage < 0, keep the current percentage value
     * @param msg
     * @return
     */
    private String[] getProgressMsg(int percentage, String msg) {
        String[] progressMsg = new String[2];
        // Validate
        if (percentage < 0) {
            // Keep current percentage value
            percentage = mDatabaseUpdateProgressBar.getProgress();

        } else if (percentage > 100) {
            percentage = 100;
        }
        // Construct
        progressMsg[0] = ((Integer)(percentage)).toString();
        progressMsg[1] = msg;

        return progressMsg;
    }

    /**
     * Operations will be done before downloading task
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Hide the "Start" Button
        mDatabaseUpdateStartButton.setVisibility(View.GONE);
    }

    /**
     * Operations will be done after downloading task
     * @param result
     */
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (result) {
            // Successfully completed -> close this activity and refresh
            mParentActivity.finishAndRefresh();

        } else {
            // Failed
            if (mStartOption == 0) {
                // Show the "Start" button again
                mDatabaseUpdateStartButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Update downloading progress
     * @param progressValue [0]: total progress from 0 ~ 100, [1]: progress message
     */
    @Override
    protected void onProgressUpdate(String... progressValue) {
        super.onProgressUpdate(progressValue);

        // Update progress bar
        if (progressValue != null && progressValue.length > 1) {
            // Update progress bar
            mDatabaseUpdateProgressBar.setProgress(Integer.parseInt(progressValue[0]));
            // Update progress message (status)
            mDatabaseUpdateStatusTextView.setText(progressValue[1]);
        }
    }
}

