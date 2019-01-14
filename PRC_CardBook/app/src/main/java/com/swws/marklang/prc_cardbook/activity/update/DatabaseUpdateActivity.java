package com.swws.marklang.prc_cardbook.activity.update;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;


public class DatabaseUpdateActivity extends AppCompatActivity {

    public static final String KEY_START_OPTION = "com.swws.marklang.prc_cardbook.START_OPTION";

    private int mStartOption; // 0: Start by user, 1: Start by app (in the first launching).
    private DatabaseUpdateDownloadTask mDatabaseUpdateDownloadTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_update);

        // Extract passed in info.
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_START_OPTION)) {
            mStartOption = intent.getExtras().getInt(KEY_START_OPTION);
        } else {
            Log.e(this.getClass().getName(), KEY_START_OPTION + " NOT FOUND!");
            return;
        }

        // Initialize UI components
        initUIs();

        // NOTE: The AlertDialog will only be shown after onCreate() finishes
        // Check Network connection
        if (!isNetworkConnected()) {
            // No Network connection
            AlertDialog noNetworkAlertDialog = createNoNetworkAlertDialog();
            noNetworkAlertDialog.show();

        } else {
            // Check WIFI connection
            if (!isWifiConnected()) {
                // No WIFI connection - Pop up an alert
                AlertDialog noWifiAlertDialog = createWIFIAlertDialog();
                noWifiAlertDialog.show();

            } else {
                // All exceptions passed
                if (mStartOption == 1) {
                    // This activity is started by the app.
                    AlertDialog firstRunDialog = createFirstRunDialog();
                    firstRunDialog.show();
                }
            }
        }
    }

    /**
     * Init UI components
     */
    private void initUIs() {
        // Set Title
        if (mStartOption == 0) {
            // Local Database Update
            setTitle(R.string.database_update_activity_name);
        } else {
            // Download all database in the first time
            setTitle(R.string.first_database_update_activity_name);
        }

        // Get the "Start" button
        Button databaseUpdateStartButton = (Button) findViewById(R.id.databaseUpdateStartButton);

        // Check is started by the app.
        if (mStartOption != 1) {

            // Show and Configure the "Start" button
            databaseUpdateStartButton.setVisibility(View.VISIBLE);
            databaseUpdateStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseUpdateActivity.this.startUpdateThread();
                }
            });

            // Display Back Button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        } else {
            // Hide the "Start" button
            databaseUpdateStartButton.setVisibility(View.GONE);

            // Hide Back Button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Start the update thread to do update task
     */
    private void startUpdateThread() {
        // Get the "Start" button
        Button databaseUpdateStartButton = (Button) findViewById(R.id.databaseUpdateStartButton);
        // Get ProgressBar
        ProgressBar databaseUpdateProgressBar = (ProgressBar) findViewById(R.id.databaseUpdateProgressBar);
        // Get TextView
        TextView databaseUpdateStatusTextView = (TextView) findViewById(R.id.databaseUpdateStatusTextView);
        // Create and Start update the database
        mDatabaseUpdateDownloadTask = new DatabaseUpdateDownloadTask(
                DatabaseUpdateActivity.this,
                databaseUpdateStartButton,
                databaseUpdateProgressBar,
                databaseUpdateStatusTextView,
                mStartOption
        );
        mDatabaseUpdateDownloadTask.execute();
    }

    /**
     * Close this activity And Refresh the ListDatabases used in MainActivity
     */
    public void finishAndRefresh() {
        setResult(RESULT_OK); // Notify the caller to update
        finish();
    }

    /**
     * Close this activity And notify MainActivity that database updating was failed
     */
    public void finishAndDoNothing() {
        setResult(RESULT_CANCELED); // Notify the caller the failure
        finish();
    }

    /**
     * Back Button on the Navigation Bar is Pressed
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        // Check is the background downloading thread terminated
        if (mDatabaseUpdateDownloadTask != null) {
            mDatabaseUpdateDownloadTask.cancel(true);
        }

        // Close this activity
        finishAndDoNothing();
        return true;
    }

    /**
     * Back Button is Pressed
     */
    @Override
    public void onBackPressed() {
        // Check is the background downloading thread terminated
        if (mDatabaseUpdateDownloadTask != null) {
            mDatabaseUpdateDownloadTask.cancel(true);
        }

        // Close this activity
        finishAndDoNothing();
    }

    /**
     * Check is network connected
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null;
    }

    /**
     * Check is WIFI connected
     * @return
     */
    private boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Create no network connection alert dialog
     * @return
     */
    private AlertDialog createNoNetworkAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.info_download_no_internet_connection_message)
                .setTitle(R.string.info_download_no_internet_connection_title);

        // Add button
        builder.setPositiveButton(R.string.button_yes_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Click Yes Button - Terminate this activity
                finishAndDoNothing();
            }
        });

        // Create this dialog
        return builder.create();
    }

    /**
     * Create no WIFI connection alert dialog
     * @return
     */
    private AlertDialog createWIFIAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.info_download_no_wifi_message)
                .setTitle(R.string.info_download_no_wifi_title);

        // Add buttons
        builder.setPositiveButton(R.string.button_yes_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Click Yes Button - Maybe go to the task of updating
                if (mStartOption == 1) {
                    // This activity is started by the app.
                    startUpdateThread();
                }
            }
        });
        builder.setNegativeButton(R.string.button_no_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Click No Button - Terminate this activity
                finishAndDoNothing();
            }
        });

        // Create this dialog
        return builder.create();
    }

    /**
     * Create "database download in the first run" alert dialog
     * @return
     */
    private AlertDialog createFirstRunDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.info_download_first_time_download_database_message)
                .setTitle(R.string.info_download_first_time_download_database_title);

        // Add buttons
        builder.setPositiveButton(R.string.button_yes_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Click Yes Button - Go to the task of updating
                startUpdateThread();
            }
        });
        builder.setNegativeButton(R.string.button_no_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Click No Button - Terminate this activity
                finishAndDoNothing();
            }
        });

        // Create this dialog
        return builder.create();
    }
}
