package com.swws.marklang.prc_cardbook.utility.concurrent;

import java.util.LinkedList;

public class ConcurrentTaskController implements ConcurrentTaskControllerCallBack {

    private int mMaxCountRunningTask;
    private LinkedList<ConcurrentTask> mTaskPool;

    private int mCurrentRunningTask;


    /**
     * Constructor
     * @param maxCountRunningTask Maximum number of tasks that can be run concurrently
     */
    public ConcurrentTaskController(int maxCountRunningTask) {
        mMaxCountRunningTask = maxCountRunningTask;

        mTaskPool = new LinkedList<>();
        mCurrentRunningTask = 0;
    }


    /**
     * Execute a ConcurrentTask
     * If the count of current running tasks less than the maximum, execute it immediately;
     * Otherwise, this task will be pended until other tasks are finished.
     * @param task
     */
    public void executeTask(ConcurrentTask task) {
        if (mCurrentRunningTask < mMaxCountRunningTask) {
            // This new added task can be executed
            mCurrentRunningTask += 1;
            task.execute();

        } else {
            // This new added task need to be pended
            mTaskPool.addLast(task);
        }
    }

    /**
     * Remove all pending ConcurrentTasks
     */
    public void removeAllPendingTasks() {
        mTaskPool.clear();
    }

    /**
     * Should always be called after each ConcurrentTask finishes
     */
    @Override
    public void onFinish() {
        if (!mTaskPool.isEmpty()) {
            // There are pending tasks in the TaskPool
            ConcurrentTask nextTask = mTaskPool.remove();

            nextTask.execute();

        } else {
            // There is no pending task in the TaskPool
            mCurrentRunningTask -= 1;
        }
    }
}
