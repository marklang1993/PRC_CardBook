package com.swws.marklang.prc_cardbook.activity.system;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainLoadActivity;
import com.swws.marklang.prc_cardbook.activity.system.inventory.ExportInventoryDatabaseTask;
import com.swws.marklang.prc_cardbook.activity.system.inventory.ImportInventoryDatabaseTask;

import java.io.File;

public class SystemProgressActivity extends AppCompatActivity {

    // TODO
    private static final String BACKUP_FILE_NAME = "backup.prc";

    private ProgressBar mProgressBar;
    private int mTaskIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_progress);

        // Get task index
        Intent intent = getIntent();
        if (intent.hasExtra(SystemActivity.KEY_SYSTEM_TASK_INDEX)) {
            // Retrieve task index
            mTaskIndex = intent.getExtras().getInt(SystemActivity.KEY_SYSTEM_TASK_INDEX);

        } else {
            // No task index passed
            Log.e(this.getClass().getName(), SystemActivity.KEY_SYSTEM_TASK_INDEX + " NOT FOUND!");
            return;
        }


        // Init. Progress Bar
        mProgressBar = (ProgressBar) findViewById(R.id.systemProgressProgressBar);
        updateProgress(0);

        // Init. UIs
        setTitle(R.string.system_activity_name);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO: Show confirm dialog?

        // Start corresponding task
        switch (mTaskIndex) {
            case SystemActivity.SYSTEM_TASK_EXPORT_INVENTORY_DATABASE:
                // TODO: need to request WRITE_EXTERNAL_STORAGE permission

                ExportInventoryDatabaseTask exportInventoryDBTask = new ExportInventoryDatabaseTask(
                        this,
                        BACKUP_FILE_NAME
                );
                exportInventoryDBTask.execute();
                break;

            case SystemActivity.SYSTEM_TASK_IMPORT_INVENTORY_DATABASE:
                // TODO: need to request READ_EXTERNAL_STORAGE permission

                ImportInventoryDatabaseTask importInventoryDBTask = new ImportInventoryDatabaseTask(
                        this,
                        BACKUP_FILE_NAME
                );
                importInventoryDBTask.execute();
                break;

            default:
                // Error: Not such task index
                Log.e(this.getClass().getName(),
                        String.format("KEY_SYSTEM_TASK_INDEX %d is illegal!", mTaskIndex));
                finish();
                break;
        }
    }

    /**
     * Called after the AsyncTask finished
     * @param result
     */
    public void notifyResult(boolean result) {
        if (result) {
            // Successful
            setResult(RESULT_OK);

        } else {
            // Error occurred
            setResult(RESULT_CANCELED);
        }

        // Close this activity
        finish();
    }

    /**
     * Update the display of current progress
     * @param progress
     */
    public void updateProgress(int progress) {

        // Validate parameter
        if (progress < 0) {
            progress = 0;

        } else if (progress > 100) {
            progress = 100;

        }
        // Update the progressbar
        mProgressBar.setProgress(progress);
    }
}
