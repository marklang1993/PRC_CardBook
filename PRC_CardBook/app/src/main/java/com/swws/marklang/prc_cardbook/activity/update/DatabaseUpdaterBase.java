package com.swws.marklang.prc_cardbook.activity.update;

import android.content.Context;
import android.util.Log;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.HttpUtility;
import com.swws.marklang.prc_cardbook.utility.MathUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

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
        mHttpUtility = new HttpUtility();
        mFileUtility = new FileUtility(mContext);
    }

    /**
     * Get all images of all items
     * @param urlPrefix
     * @param seasonID
     * @param databases
     * @return
     * @throws HttpUtility.DirCreateException
     * @throws IOException
     */
    protected boolean getItemImages(
            String urlPrefix,
            SeasonID seasonID,
            LinkedList<Database> databases
    )
            throws HttpUtility.DirCreateException, IOException
    {
        HashSet<String> allImageNames = new HashSet<>(); // For debug purpose

        // Update the initial value of the progress bar
        mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(mProgressValues[2],
                mContext.getString(R.string.info_download_coordinate_images))
        );

        // Calculate the total count of images
        int totalCount = 0;
        for (Database database : databases) {
            totalCount += database.size();
        }

        // Populate all databases
        int cursorItem = 0;
        int cursorList = 0;
        for (Database database : databases) {

            String currentUrlPrefix;
            /*
             *  Since SEASON_2ND, the sub-directory of the resource is given by
             *  the series prefix of the key(url) of "Database", such as "J01", "J02"..
             */
            if (seasonID != SeasonID.SEASON_1ST) {
                // Get each URL
                ArrayList<String> urlList = DatabaseUpdater2.GetUrlList(database.url());
                /*
                 *  Since all urls in the same Database share the same series prefix,
                 *  use the 1st element is ok.
                 */
                String urlElement = urlList.get(0);
                int firstSlashIndex = urlElement.indexOf('/');
                String seriesPrefix = urlElement.substring(0, firstSlashIndex + 1);
                currentUrlPrefix = urlPrefix + seriesPrefix;

            } else {
                // SEASON_1ST
                currentUrlPrefix = urlPrefix;

            }


            // Populate all items
            int cursorDataBase = 0;
            for (Item item: database) {

                // ## Check whether this task is cancelled.
                if (mDownLoadTask.GetCancelStatus()) {
                    return false;
                }

                // Update the value of the progress bar
                int currentProgress = MathUtility.calculateCurrentProgressValue(
                        cursorItem, 0, totalCount, mProgressValues[2], mProgressValues[3] - 1
                );
                mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(currentProgress,
                        mContext.getString(R.string.info_download_coordinate_images) + "\n" + item.ItemName
                        )
                );

                // Download ItemImage
                mFileUtility.DownloadImage(
                        allImageNames,
                        currentUrlPrefix,
                        item.ItemImage,
                        FileUtility.IMAGE_TYPE.IMAGE,
                        seasonID,
                        mHttpUtility,
                        mIsPrintDebug
                );
                // Download BrandImage
                mFileUtility.DownloadImage(
                        null,
                        currentUrlPrefix,
                        item.Brand,
                        FileUtility.IMAGE_TYPE.BRAND,
                        seasonID,
                        mHttpUtility,
                        mIsPrintDebug
                );
                // Download TypeImage
                mFileUtility.DownloadImage(
                        null,
                        currentUrlPrefix,
                        item.Type,
                        FileUtility.IMAGE_TYPE.TYPE,
                        seasonID,
                        mHttpUtility,
                        mIsPrintDebug
                );

                // Print current progress
                if (mIsPrintDebug)
                {
                    Log.i(this.getClass().getSimpleName(),
                            String.format("DataBase: %d / %d; Items: %d / %d",
                                    cursorList + 1, databases.size(),
                                    cursorDataBase + 1, database.size())
                    );
                }
                ++cursorItem;
                ++cursorDataBase;
            }
            ++cursorList;
        }

        // Print total progress
        Log.e(this.getClass().getSimpleName(), String.format("Total Images Count: %d", allImageNames.size()));
        return true;
    }
}
