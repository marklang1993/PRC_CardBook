package com.swws.marklang.prc_cardbook.activity.update;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseFileUtility;
import com.swws.marklang.prc_cardbook.utility.HttpUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class DatabaseUpdateDownloadTask extends AsyncTask<Void, String, Boolean> {

    private DatabaseUpdateActivity mParentActivity;
    private Context mContext;

    private Button mDatabaseUpdateStartButton;
    private ProgressBar mDatabaseUpdateProgressBar;
    private TextView mDatabaseUpdateStatusTextView;
    private ArrayList<CheckBox> mCheckBoxArrayList;

    private DatabaseFileUtility mDatabaseFileUtility;

    private ArrayList<IDatabaseUpdater> mUpdaterList;

    /**
     * Index: Message
     * 0    : info_download_write_meta_file
     * 1    : info_download_complete
     */
    private int[] mFinalProgressValues = {99, 100};

    private int mStartOption; // 0: Start by user, 1: Start by app (in the first launching).

    // For debug purpose
    protected final boolean mIsPrintDebug = true;


    public DatabaseUpdateDownloadTask(
            DatabaseUpdateActivity parentActivity,
            Button databaseUpdateStartButton,
            ProgressBar databaseUpdateProgressBar,
            TextView databaseUpdateStatusTextView,
            ArrayList<CheckBox> checkBoxArrayList,
            int startOption
    ) {
        super();

        // Get parent activity
        mParentActivity = parentActivity;
        // Get Context
        mContext = mParentActivity.getApplicationContext();

        // Init. File Utilities
        mDatabaseFileUtility = DatabaseFileUtility.getInstance();

        // Get StartOption
        mStartOption = startOption;

        // Update references of UI components
        mDatabaseUpdateStartButton = databaseUpdateStartButton;
        mDatabaseUpdateProgressBar = databaseUpdateProgressBar;
        mDatabaseUpdateStatusTextView = databaseUpdateStatusTextView;
        mCheckBoxArrayList = checkBoxArrayList;

        // Init. All Updaters
        mUpdaterList = new ArrayList<>();
        // TODO: find a better structure to handle this logic
        // Season 1
        if (mCheckBoxArrayList.get(0).isChecked()){
            mUpdaterList.add(new DatabaseUpdater1(this));
        }
        // Season 2
        if (mCheckBoxArrayList.get(1).isChecked()){
            mUpdaterList.add(new DatabaseUpdater2(this));
        }
    }

    /**
     * Construct a Progress Message Array
     * @param percentage if percentage < 0, keep the current percentage value
     * @param msg
     * @return
     */
    public String[] GetProgressMsg(int percentage, String msg) {
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
     * Get current context
     * @return Context Object
     */
    public Context GetContext() { return mContext; }

    /**
     * Get the status of cancellation
     * @return
     */
    public boolean GetCancelStatus() { return isCancelled(); }

    /**
     * Publish Progress to UIs
     * @param msgs
     */
    public void PublishProgress(String[] msgs) { publishProgress(msgs); }

    /**
     * Downloading task is executed here
     * @param voids
     * @return
     */
    @Override
    protected Boolean doInBackground(Void... voids) {

        // Get current Databases as oldDatabases
        ArrayList<Database> oldDatabases = MainActivity.getAllDatabases();
        // All new Databases
        LinkedList<Database> allNewDatabases = new LinkedList<>();

        // Start to update local Databases
        try {

            // Execute the updater in each season
            for (IDatabaseUpdater updater: mUpdaterList) {
                // 1. Get the directory of all urls
                LinkedHashMap<String, String> urlDict = updater.GetUrlDict(oldDatabases);
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
                    LinkedList<Database> newDatabases = updater.GetDatabaseLinkedList(urlDict);
                    // ## Check whether this task is cancelled inside the function.
                    if (newDatabases == null) {
                        return false;
                    }
                    // ## Check whether this task is cancelled.
                    if (isCancelled()) {
                        return false;
                    }

                    // 3. Get new item images
                    boolean isFinished = updater.GetItemImages(newDatabases);
                    // ## Check whether this task is cancelled inside the function.
                    if (!isFinished) {
                        return false;
                    }

                    // Put newDatabase into allNewDatabases
                    allNewDatabases.addAll(newDatabases);
                }
            }

            // 4. Flush all new Databases to the metadata file
            writeNewMetaData(oldDatabases, allNewDatabases);

            // Show the final progress status
            publishProgress(GetProgressMsg(mFinalProgressValues[1],
                    mContext.getString(R.string.info_download_complete))
            );

            // Delay for a while, and then terminate this thread
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) { }

        } catch (HttpUtility.ServerErrorException e) {
            // Server Error
            publishProgress(GetProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_official_server_malfunction))
            );
            return false;

        } catch (HttpUtility.DirCreateException e) {
            // Cannot Create Direction
            publishProgress(GetProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_directory_creation_failed))
            );
            return false;

        } catch (MalformedURLException e) {
            // URL is Incorrect
            publishProgress(GetProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_app_update_required))
            );
            return false;

        } catch (IOException e) {
            // Connection Failed
            publishProgress(GetProgressMsg(-1,
                    mContext.getString(R.string.info_download_error_no_internet))
            );
            return false;
        }

        return true;
    }

    /**
     * 4th Step: Write new metadata
     * @param oldDatabases old local Databases
     * @param newDatabases new Databases retrieved from official website
     */
    private void writeNewMetaData(ArrayList<Database> oldDatabases, LinkedList<Database> newDatabases) {
        // Update the initial value of the progress bar
        publishProgress(GetProgressMsg(mFinalProgressValues[0],
                mContext.getString(R.string.info_download_write_meta_file))
        );

        // Inherit old data from "oldDatabases"
        if (oldDatabases != null) {
            // Move all databases from "oldDatabases" to inheritDatabases
            LinkedList<Database> inheritDatabases = new LinkedList<>();
            for (Database oldDatabase : oldDatabases) {
                // Check is there any entry with same key or same value in both "oldDatabases" and "newDatabases"
                if (!newDatabases.contains(oldDatabase)) {
                    inheritDatabases.add(oldDatabase);
                }
                /*
                 * If True, discard the current entry in the oldDatabases since
                 * 1. it may be duplicated with the entry in newDatabases;
                 * 2. it may be outdated.
                 */
            }

            // Merge inheritDatabases and newDatabases
            newDatabases.addAll(inheritDatabases);
        }

        // Go to write metadata
        mDatabaseFileUtility.WriteAllMetaData(newDatabases, mIsPrintDebug);
    }

    /**
     * Operations will be done before downloading task
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Hide the "Start" Button
        mDatabaseUpdateStartButton.setVisibility(View.GONE);
        // Hide all CheckBox
        for (CheckBox checkBox :mCheckBoxArrayList) {
            checkBox.setVisibility(View.GONE);
        }

    }

    /**
     * Operations will be done after downloading task
     * @param result
     */
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (result) {
            // Successfully completed

            // Clear all references to the UI objects. TODO: find a better way to handle these references
            mDatabaseUpdateStartButton = null;
            mDatabaseUpdateProgressBar = null;
            mDatabaseUpdateStatusTextView = null;
            mCheckBoxArrayList = null;

            // Close this activity and refresh
            mParentActivity.finishAndRefresh();

        } else {
            // Failed
            if (mStartOption == 0) {
                // Show the "Start" button again
                mDatabaseUpdateStartButton.setVisibility(View.VISIBLE);
                // Show all CheckBox
                for (CheckBox checkBox :mCheckBoxArrayList) {
                    checkBox.setVisibility(View.VISIBLE);
                }
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

