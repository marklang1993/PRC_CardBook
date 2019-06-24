package com.swws.marklang.prc_cardbook.activity.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.activity.update.DatabaseUpdateActivity;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseFileUtility;

public class MainLoadActivity extends AppCompatActivity {

    private static Context mApplicationContext;

    public static final String KEY_INIT_DB_OPTION = "com.swws.marklang.prc_cardbook.INIT_DB_OPTION";
    private boolean isStartedByMain;

    private DatabaseFileUtility mDatabaseFileUtility;
    private MainLoadTask mMainLoadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_load);

        // Get application context
        mApplicationContext = getApplicationContext();

        // Init. UIs
        setTitle(getString(R.string.main_load_activity_name));

        // Extract passed in info.
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_INIT_DB_OPTION)) {
            // Started by MainActivity (after local Database updated)
            isStartedByMain = true;

        } else {
            // Started once the app is launched
            isStartedByMain = false;
        }

        // Check is metadata presented
        mDatabaseFileUtility = DatabaseFileUtility.getInstance();
        if (!mDatabaseFileUtility.IsMetadataFilePresented()) {
            // Have to start "local database update" activity to download all initial data
            Intent databaseUpdateActivityIntent = new Intent(getApplicationContext(), DatabaseUpdateActivity.class);
            databaseUpdateActivityIntent.putExtra(DatabaseUpdateActivity.KEY_START_OPTION, 1); // "1" means it is started by app.
            startActivityForResult(databaseUpdateActivityIntent, Constants.REQUEST_UPDATE_RESULT_APP);

        } else {
            // Init. data
            initData();
        }
    }

    /**
     * Callback function for MainLoadTask
     * @param result
     */
    public void Finished(boolean result) {
        if (result) {
            // Loading succeed - Init. MainActivity
            if (!isStartedByMain) {
                // Started once the app is launched
                Intent mainActivityLaunchIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityLaunchIntent);
            } else {
                // Started by MainActivity (after local Database updated)
                setResult(RESULT_OK);
            }

            // Terminate this activity
            finish();

        } else {
            // Loading failed - Terminate the app.
            Log.e(this.getClass().getName(), "Loading Data Failed!");
            finishAndRemoveTask(); // MUST be used since API 21
        }
    }

    /**
     * Init. InventoryDB and item Database.
     */
    private void initData() {

        // Get ProgressBar
        ProgressBar mainLoadProgressBar = (ProgressBar) findViewById(R.id.mainLoadProgressBar);

        // Start the loading task
        mMainLoadTask = new MainLoadTask(this, mainLoadProgressBar);
        mMainLoadTask.execute();
    }

    /**
     * Back Button is Pressed
     */
    @Override
    public void onBackPressed() {
        // Check is the background loading thread terminated
        if (mMainLoadTask != null) {
            // Wait until the download task is exited
            try {
                mMainLoadTask.get();
            } catch (Exception ex) {
                ;
            }
        }

        // Terminate the app.
        Log.e(this.getClass().getName(), "Loading Data is Terminated by User!");
        finishAndRemoveTask(); // MUST be used since API 21
    }

    /**
     * Receive the Result from "DatabaseUpdateActivity"
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_UPDATE_RESULT_APP) {
            // Check is database updating successful
            if (resultCode == RESULT_OK) {
                // Update succeed - Initialize all data.
                initData();

            } else {
                // Update failed - Terminate the app.
                finishAndRemoveTask(); // MUST be used since API 21
            }
        }
    }

    /**
     * Get current application context
     * @return
     */
    public static Context getCurrentApplicationContext() {
        return mApplicationContext;
    }
}
