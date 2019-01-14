package com.swws.marklang.prc_cardbook.activity.update;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;


public class DatabaseUpdateActivity extends AppCompatActivity {

    private DatabaseUpdateDownloadTask mDatabaseUpdateDownloadTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_update);

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
            }
        }

        // Init.
        initUIs();
    }

    /**
     * Init UI components
     */
    private void initUIs() {
        // Set Title
        setTitle(R.string.database_update_activity_name);

        // Start Database Update
        final Button databaseUpdateStartButton = (Button) findViewById(R.id.databaseUpdateStartButton);
        databaseUpdateStartButton.setVisibility(View.VISIBLE);
        databaseUpdateStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get ProgressBar
                ProgressBar databaseUpdateProgressBar = (ProgressBar) findViewById(R.id.databaseUpdateProgressBar);
                // Get TextView
                TextView databaseUpdateStatusTextView = (TextView) findViewById(R.id.databaseUpdateStatusTextView);
                // Create and Start update the database
                mDatabaseUpdateDownloadTask = new DatabaseUpdateDownloadTask(
                        getApplicationContext(),
                        databaseUpdateStartButton,
                        databaseUpdateProgressBar,
                        databaseUpdateStatusTextView
                );
                mDatabaseUpdateDownloadTask.execute();
            }
        });

        // Display Back Button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        // Check is the background downloading thread terminated
        if (mDatabaseUpdateDownloadTask != null) {
            mDatabaseUpdateDownloadTask.cancel(true);
        }
        // Close this activity
        finish();
        return true;
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
                finish();
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
                // Click Yes Button - Do nothing
            }
        });
        builder.setNegativeButton(R.string.button_no_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Click No Button - Terminate this activity
                finish();
            }
        });

        // Create this dialog
        return builder.create();
    }
}
