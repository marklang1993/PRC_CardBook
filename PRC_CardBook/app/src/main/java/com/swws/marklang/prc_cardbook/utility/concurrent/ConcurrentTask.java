package com.swws.marklang.prc_cardbook.utility.concurrent;

import android.os.AsyncTask;

public abstract class ConcurrentTask<Progress, Result> extends AsyncTask<Void, Progress, Result> {

    private ConcurrentTaskControllerCallBack mControllerCallBack;

    public ConcurrentTask(ConcurrentTaskControllerCallBack controllerCallBack) {
        mControllerCallBack = controllerCallBack;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);

        // Notify the controller
        mControllerCallBack.onFinish();
    }

    /**
     * Execute this ConcurrentTask
     * by calling its super class function
     */
    public void execute() {
        super.execute();
    }
}
