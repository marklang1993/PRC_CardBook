package com.swws.marklang.prc_cardbook.activity.system;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class SystemActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    // Some constants
    public static final String KEY_SYSTEM_TASK_INDEX = "com.swws.marklang.prc_cardbook.SYSTEM_TASK_INDEX";
    public static final int SYSTEM_TASK_EXPORT_INVENTORY_DATABASE = 1;
    public static final int SYSTEM_TASK_IMPORT_INVENTORY_DATABASE = 2;
    public static final int SYSTEM_TASK_CLEAR_INVENTORY_DATABASE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);

        // Init. UIs
        setTitle(R.string.system_activity_name);
        initButtons();
    }

    /**
     * Check and request corresponding permission via EasyPermissions
     * @param systemTaskIndex System Task Index
     * @param resultRequestCode Request Code for getting activity result
     * @param permission string of required permission
     * @param requestPermissionRationale rationale string for requesting permission from user
     * @param permissionRequestCode Request Code for getting permission
     */
    private void checkPermission(
            int systemTaskIndex,
            int resultRequestCode,
            String permission,
            String requestPermissionRationale,
            int permissionRequestCode) {

        if (EasyPermissions.hasPermissions(this, permission)) {
            // The corresponding permission has already been grant - start the corresponding task
            startTask(systemTaskIndex, resultRequestCode);

        } else {
            // Request read external storage permission
            EasyPermissions.requestPermissions(
                    this,
                    requestPermissionRationale,
                    permissionRequestCode,
                    permission
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
     * The corresponding permission is granted
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // The permission is granted, start the corresponding task
        switch (requestCode) {
            case Constants.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_EXPORT_INV_DB:
                // Export Inventory Database
                startTask(SYSTEM_TASK_EXPORT_INVENTORY_DATABASE, Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_EXPORTING);
                break;

            case Constants.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_IMPORT_INV_DB:
                // Import Inventory Database
                startTask(SYSTEM_TASK_IMPORT_INVENTORY_DATABASE, Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_IMPORTING);
                break;
        }
    }

    /**
     * The corresponding permission request is denied
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Permission is NOT granted by the user - show an error message
        Toast.makeText(this, R.string.exception_no_permission_runtime, Toast.LENGTH_LONG)
                .show();
    }

    /**
     * Init. all buttons in this activity
     */
    private void initButtons() {
        // System Export Inventory Database Button
        Button systemExportInventoryDatabaseButton = (Button)
                findViewById(R.id.systemExportInventoryDatabaseButton);
        systemExportInventoryDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(SYSTEM_TASK_EXPORT_INVENTORY_DATABASE,
                        Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_EXPORTING,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.permission_request_rationale_write_external_storage_systemactivity),
                        Constants.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_EXPORT_INV_DB
                        );
            }
        });

        // System Import Inventory Database Button
        Button systemImportInventoryDatabaseButton = (Button)
                findViewById(R.id.systemImportInventoryDatabaseButton);
        systemImportInventoryDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(SYSTEM_TASK_IMPORT_INVENTORY_DATABASE,
                        Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_IMPORTING,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.permission_request_rationale_read_external_storage_systemactivity),
                        Constants.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_IMPORT_INV_DB
                );
            }
        });

        // System Clear Inventory Database Button
        Button systemClearInventoryDatabaseButton = (Button)
                findViewById(R.id.systemClearInventoryDatabaseButton);
        systemClearInventoryDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear inventory database does not need extra permission
                startTask(SYSTEM_TASK_CLEAR_INVENTORY_DATABASE, Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_CLEAR);
            }
        });

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
    }

    /**
     * Start system task
     * @param systemTaskIndex System Task Index
     * @param resultRequestCode System Task Result Request Code
     */
    private void startTask(int systemTaskIndex, int resultRequestCode) {
        Intent intent = new Intent(SystemActivity.this, SystemProgressActivity.class);
        intent.putExtra(KEY_SYSTEM_TASK_INDEX, systemTaskIndex);
        startActivityForResult(intent, resultRequestCode);
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * Receive the Result from invoked Activities
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_EXPORTING:
                Toast.makeText(this,
                        resultCode == RESULT_OK ?
                                R.string.system_export_inventory_database_done :
                                R.string.system_export_inventory_database_fail,
                        Toast.LENGTH_SHORT)
                        .show();
                break;

            case Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_IMPORTING:
                if (resultCode == RESULT_OK) {
                    // Importing is successful
                    Toast.makeText(this,
                            R.string.system_import_inventory_database_done,
                            Toast.LENGTH_SHORT).show();
                    // Need to ask MainActivity to update inventory database
                    setResult(RESULT_OK);

                } else {
                    // Importing is failed
                    Toast.makeText(this,
                            R.string.system_import_inventory_database_fail,
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case Constants.REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_CLEAR:
                // Clear is successful
                Toast.makeText(this,
                        R.string.system_clear_inventory_database_done,
                        Toast.LENGTH_SHORT).show();
                // Need to ask MainActivity to update inventory database
                setResult(RESULT_OK);
                break;
        }
    }
}
