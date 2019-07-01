package com.swws.marklang.prc_cardbook.activity.system;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;

public class SystemActivity extends AppCompatActivity {

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
     * Init. all buttons in this activity
     */
    public void initButtons() {
        // System Export Inventory Database Button
        Button systemExportInventoryDatabaseButton = (Button)
                findViewById(R.id.systemExportInventoryDatabaseButton);
        systemExportInventoryDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemActivity.this, SystemProgressActivity.class);
                intent.putExtra(KEY_SYSTEM_TASK_INDEX, SYSTEM_TASK_EXPORT_INVENTORY_DATABASE);
                startActivityForResult(intent, Constants.REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_EXPORTING);
            }
        });

        // System Import Inventory Database Button
        Button systemImportInventoryDatabaseButton = (Button)
                findViewById(R.id.systemImportInventoryDatabaseButton);
        systemImportInventoryDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemActivity.this, SystemProgressActivity.class);
                intent.putExtra(KEY_SYSTEM_TASK_INDEX, SYSTEM_TASK_IMPORT_INVENTORY_DATABASE);
                startActivityForResult(intent, Constants.REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_IMPORTING);
            }
        });

        // System Clear Inventory Database Button
        Button systemClearInventoryDatabaseButton = (Button)
                findViewById(R.id.systemClearInventoryDatabaseButton);
        systemClearInventoryDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemActivity.this, SystemProgressActivity.class);
                intent.putExtra(KEY_SYSTEM_TASK_INDEX, SYSTEM_TASK_CLEAR_INVENTORY_DATABASE);
                startActivityForResult(intent, Constants.REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_CLEAR);
            }
        });

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
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
            case Constants.REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_EXPORTING:
                Toast.makeText(this,
                        resultCode == RESULT_OK ?
                                R.string.system_export_inventory_database_done :
                                R.string.system_export_inventory_database_fail,
                        Toast.LENGTH_SHORT)
                        .show();
                break;

            case Constants.REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_IMPORTING:
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

            case Constants.REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_CLEAR:
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
