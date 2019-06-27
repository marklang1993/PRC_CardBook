package com.swws.marklang.prc_cardbook.activity.system.inventory;

import android.os.AsyncTask;

import com.swws.marklang.prc_cardbook.activity.system.SystemProgressActivity;

public abstract class ExternalFileOperationInventoryDatabaseTaskBase extends AsyncTask<Void, Integer, Boolean> {

    protected SystemProgressActivity mProgressDisplayActivity;
    protected String mFileName;
    protected boolean isCancelled;

    public ExternalFileOperationInventoryDatabaseTaskBase(
            SystemProgressActivity progressDisplayActivity,
            String fileName
    ) {
        mProgressDisplayActivity = progressDisplayActivity;
        mFileName = fileName;

        isCancelled = false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        // Notify the caller
        mProgressDisplayActivity.notifyResult(aBoolean);

        // Do some clean
        mProgressDisplayActivity = null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mProgressDisplayActivity.updateProgress(values[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        isCancelled = true;
    }

    /**
     * For InventoryFileUtility to update progress
     * @param progress current progress
     */
    public void postNewProgress(int progress) {
        publishProgress(progress);
    }
}
