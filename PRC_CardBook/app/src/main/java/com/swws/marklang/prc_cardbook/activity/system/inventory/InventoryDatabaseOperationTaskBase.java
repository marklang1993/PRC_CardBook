package com.swws.marklang.prc_cardbook.activity.system.inventory;

import android.os.AsyncTask;

import com.swws.marklang.prc_cardbook.activity.system.SystemProgressActivity;


abstract class InventoryDatabaseOperationTaskBase extends AsyncTask<Void, Integer, Boolean> {

    protected SystemProgressActivity mProgressDisplayActivity;
    protected boolean isCancelled;


    InventoryDatabaseOperationTaskBase(
            SystemProgressActivity progressDisplayActivity
    ) {
        mProgressDisplayActivity = progressDisplayActivity;
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
