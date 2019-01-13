package com.swws.marklang.prc_cardbook.activity.update;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DatabaseUpdateDownloadTask extends AsyncTask<Void, String, Boolean> {

    private Button mDatabaseUpdateStartButton;
    private ProgressBar mDatabaseUpdateProgressBar;
    private TextView mDatabaseUpdateStatusTextView;

    public DatabaseUpdateDownloadTask(
            Button databaseUpdateStartButton,
            ProgressBar databaseUpdateProgressBar,
            TextView databaseUpdateStatusTextView
    ) {
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
//        for (int i = 0; i < 100; ++i) {
//            String[] msg = new String[2];
//            msg[0] = ((Integer)(i + 1)).toString();
//            msg[1] = "msg: " + ((Integer)(i + 1)).toString();
//            publishProgress(msg);
//
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                ;
//            }
//        }

        return true;
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

        // Show the "Start" Button
        mDatabaseUpdateStartButton.setVisibility(View.VISIBLE);
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

