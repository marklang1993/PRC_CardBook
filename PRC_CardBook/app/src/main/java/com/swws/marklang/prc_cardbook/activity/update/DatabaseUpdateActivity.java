package com.swws.marklang.prc_cardbook.activity.update;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class DatabaseUpdateActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static final String KEY_START_OPTION = "com.swws.marklang.prc_cardbook.START_OPTION";
    private final String[] mPermissions = {Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET};

    private int mStartOption; // 0: Start by user, 1: Start by app (in the first launching).
    private DatabaseUpdateDownloadTask mDatabaseUpdateDownloadTask = null;

    // List of all CheckBox
    private ArrayList<CheckBox> mCheckBoxArrayList = null;

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

        // Check network permissions
        checkNetworkPermissions();
    }

    /**
     * Check and request INTERNET & ACCESS_NETWORK_STATE permissions via EasyPermissions
     */
    private void checkNetworkPermissions() {
        if (EasyPermissions.hasPermissions(this, mPermissions)) {
            // Network permissions have already been grant
            checkNetworkStatus(); // NOTE: The AlertDialog will only be shown after onCreate() finishes

        } else {
            // Request network permissions
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.permission_request_rationale_network),
                    Constants.REQUEST_PERMISSION_NETWORK,
                    mPermissions
            );
        }
    }

    /**
     * Get result of requesting permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Network permissions are granted
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Constants.REQUEST_PERMISSION_NETWORK && perms.size() == mPermissions.length) {
            // NOTE: The AlertDialog will only be shown after onCreate() finishes
            checkNetworkStatus();
        }
    }

    /**
     * Network permissions request is denied
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Permissions are NOT granted by the user - show an error message
        Toast.makeText(this, R.string.exception_no_permission_runtime, Toast.LENGTH_LONG)
                .show();

        // Close this activity
        finishAndDoNothing();
    }

    /**
     * Check network status and give corresponding tips
     */
    private void checkNetworkStatus() {
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

        // Get the updaterSelect LinearLayout
        LinearLayout updaterSelectLinearLayout = (LinearLayout) findViewById(R.id.updaterSelectLinearLayout);

        // Configure CheckBox
        CheckBox checkBoxSeason1 = new CheckBox(getApplicationContext());
        checkBoxSeason1.setText(R.string.database_update_checkbox_season1);
        checkBoxSeason1.setTextColor(ContextCompat.getColor(this, R.color.black));
        checkBoxSeason1.setChecked(false);

        CheckBox checkBoxSeason2 = new CheckBox(getApplicationContext());
        checkBoxSeason2.setText(R.string.database_update_checkbox_season2);
        checkBoxSeason2.setTextColor(ContextCompat.getColor(this, R.color.black));
        checkBoxSeason2.setChecked(true);

        updaterSelectLinearLayout.addView(checkBoxSeason1);
        updaterSelectLinearLayout.addView(checkBoxSeason2);

        // Create the list of CheckBox
        mCheckBoxArrayList = new ArrayList<>();
        mCheckBoxArrayList.add(checkBoxSeason1);
        mCheckBoxArrayList.add(checkBoxSeason2);

        // Ensure started by user
        if (mStartOption != 1) {

            // Show and Configure the "Start" button
            databaseUpdateStartButton.setVisibility(View.VISIBLE);
            databaseUpdateStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check whether at least 1 updater is chosen
                    boolean result = false;
                    for (CheckBox checkBox: mCheckBoxArrayList) {
                        result |= checkBox.isChecked();
                    }

                    if (result) {
                        // At least 1 updater is chosen
                        DatabaseUpdateActivity.this.startUpdateThread();

                    } else {
                        // No updater is chosen
                        createNoUpdaterAlertDialog().show();
                    }
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
                mCheckBoxArrayList,
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
            // Wait until the download task is exited
            try {
                mDatabaseUpdateDownloadTask.get();
            } catch (Exception ex) {
                ;
            }
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
            // Wait until the download task is exited
            try {
                mDatabaseUpdateDownloadTask.get();
            } catch (Exception ex) {
                ;
            }
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
     * Set the given AlertDialog cannot be cancelled.
     * @param builder
     * @return
     */
    private AlertDialog setAlertDialogNonCancellable(AlertDialog.Builder builder) {

        // Create this dialog
        AlertDialog alertDialog = builder.create();

        // Set this dialog cannot be cancelled.
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        return alertDialog;
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

        return setAlertDialogNonCancellable(builder);
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

        return setAlertDialogNonCancellable(builder);
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

        return setAlertDialogNonCancellable(builder);
    }

    /**
     * Create no updater is selected alert dialog
     * @return
     */
    private AlertDialog createNoUpdaterAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.info_download_no_updater_selected_message)
                .setTitle(R.string.info_download_no_updater_selected_title);

        // Add button
        builder.setPositiveButton(R.string.button_yes_text, null);

        return builder.create();
    }
}
