package com.swws.marklang.prc_cardbook.activity.update;

import android.content.Context;

import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.HttpUtility;

public class DatabaseUpdaterBase {

    protected DatabaseUpdateDownloadTask mDownLoadTask;
    protected Context mContext;

    protected HttpUtility mHttpUtility;
    protected FileUtility mFileUtility;

    /**
     * Index: Message
     * 0    : info_download_pages_list
     * 1    : info_download_coordinates
     * 2    : info_download_coordinate_images
     * 3    : info_download_coordinate_images (END)
     */
    protected int[] mProgressValues = {0, 5, 40, 99};

    // For debug purpose
    protected final boolean mIsPrintDebug = true;


    public DatabaseUpdaterBase(DatabaseUpdateDownloadTask downloadTask) {

        // Get Context
        mDownLoadTask = downloadTask;
        mContext = downloadTask.GetContext();

        // Init. IO Utilities
        mHttpUtility = new HttpUtility(mContext);
        mFileUtility = new FileUtility(mContext);
    }

    /**
     * Calculate the value of current progress
     * @param currentActual current value of actual value
     * @param minActual minimal value of actual value
     * @param maxActual maximal value of actual value
     * @param minProgress minimal value of the value used by ProgressBar
     * @param maxProgress maximal value of the value used by ProgressBar
     * @return
     */
    protected int calculateCurrentProgressValue(
            int currentActual,
            int minActual,
            int maxActual,
            int minProgress,
            int maxProgress
    ) {
        int rangeActual = maxActual - minActual;
        int deltaActual = currentActual - minActual;
        int rangeProgress = maxProgress - minProgress;

        double percentageActual = (double)deltaActual / (double)rangeActual;

        return (int)(rangeProgress * percentageActual) + minProgress;
    }
}
