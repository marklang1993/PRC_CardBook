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
            // Check is local database up-to-date by checking the count of series.
            if (urlDict.size() != 0)
            {
                /**
                 * NOTE: isCancel() will be checked here and inside functions of:
                 * 1. getDatabaseLinkedList();
                 * 2. getItemImages();
                 */
                // ## Check whether this task is cancelled.
                if (isCancelled()) {
                    return false;
                }

                // 2. Get databases of all series
                LinkedList<Database> databases = getDatabaseLinkedList(urlDict);
                // ## Check whether this task is cancelled inside the function.
                if (databases == null) {
                    return false;
                }
                // ## Check whether this task is cancelled.
                if (isCancelled()) {
                    return false;
                }

                // 3. Get new item images
                boolean isFinished = getItemImages(databases);
                // ## Check whether this task is cancelled inside the function.
                if (!isFinished) {
                    return false;
                }

                // 4. Write new metadata
                writeNewMetaData(oldDatabases, databases);
            }

            // Show the final progress status
            publishProgress(getProgressMsg(mProgressValues[4],
                    mContext.getString(R.string.info_download_complete))
            );

            // Delay for a while, and then terminate this thread
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

        /**
         * Iterate all entries in the "oldDatabases" and check
         * (Weak) Assert: this entry is in the rawUrlDict
         * NOTE: Even though this entry is not in the rawUrlDict, the following
         *       operations will not be affected. That's why I use "If" here.
         *
         * Algorithm:
         * If this entry has the same item size as the entry in the rawUrlDict,
         * then remove this entry from the rawUrlDict.
         */
        Iterator<Database> oldDatabasesIterator = oldDatabases.iterator();
        while (oldDatabasesIterator.hasNext())
        {
            Database oldEntry = oldDatabasesIterator.next();
            String correspondingUrl = oldEntry.url();

            if (rawUrlDict.containsKey(correspondingUrl)) { // This entry is in the rawUrlDict
                // Get the up-to-date item size of this entry
                LinkedList<String> allItemSubpageUrls = mHttpUtility.GetAllItemSubpageUrls(correspondingUrl);
                int newSize = allItemSubpageUrls.size();
                int oldSize = oldEntry.size();
                if (newSize == oldSize) { // This entry does not change at all
                    // Remove this entry from the update list
                    rawUrlDict.remove(correspondingUrl);
                }
            }
        }
        return rawUrlDict;
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
            // ## Check whether this task is cancelled.
            if (isCancelled()) {
                return null;
            }

            // Get the current entry for retrieving the url of the series page.
            Map.Entry<String, String> urlEntry = iterator.next();
            String seriesName = urlEntry.getValue();
            String seriesRelativeUrl = urlEntry.getKey();

            // Update the value of the progress bar
            int currentProgress = calculateCurrentProgressValue(
                    i, 0, countDatabases, mProgressValues[1], mProgressValues[2] - 1
            );
            publishProgress(getProgressMsg(currentProgress,
                    mContext.getString(R.string.info_download_coordinates) + " - " + seriesName
                    )
            );

            // Get the urls of the subpage of all items
            LinkedList<String> itemSubpageUrls = mHttpUtility.GetAllItemSubpageUrls(seriesRelativeUrl);

            // Generate the Database for the current series
            Database dataBase = new Database(seriesName, seriesRelativeUrl);
            for (String itemSubpageUrl: itemSubpageUrls) {
                // ## Check whether this task is cancelled.
                if (isCancelled()) {
                    return null;
                }

                // Get 1 item of this series
                Item item = mHttpUtility.PopulateItems(seriesRelativeUrl, itemSubpageUrl, mIsPrintDebug);
                dataBase.Insert(item);
            }

            // Add this Database
            databases.add(dataBase);
        }
        return databases;
    }

    /**
     * 3rd Step: Get all item images
     * @param databases
     * @return isFinished
     */
    private boolean getItemImages(LinkedList<Database> databases)
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

                // ## Check whether this task is cancelled.
                if (isCancelled()) {
                    return false;
                }

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
        return true;
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

