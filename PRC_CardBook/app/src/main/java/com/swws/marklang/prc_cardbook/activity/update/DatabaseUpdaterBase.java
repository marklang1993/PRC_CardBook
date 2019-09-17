package com.swws.marklang.prc_cardbook.activity.update;

import android.content.Context;
import android.util.Log;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.database.DatabaseFileUtility;
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

    protected DatabaseFileUtility mDatabaseFileUtility;

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

        // Init. DatabaseFileUtility
        mDatabaseFileUtility = DatabaseFileUtility.getInstance();
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
                 *  The last element of the urlList has the series prefix,
                 *  so we need to retrieve it
                 */
                String seriesPrefix = urlList.get(urlList.size() - 1);
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
                mDatabaseFileUtility.DownloadImage(
                        allImageNames,
                        currentUrlPrefix,
                        item.ItemImage,
                        DatabaseFileUtility.IMAGE_TYPE.IMAGE,
                        seasonID,
                        mIsPrintDebug
                );
                // Download BrandImage
                mDatabaseFileUtility.DownloadImage(
                        null,
                        currentUrlPrefix,
                        item.Brand,
                        DatabaseFileUtility.IMAGE_TYPE.BRAND,
                        seasonID,
                        mIsPrintDebug
                );
                // Download TypeImage
                mDatabaseFileUtility.DownloadImage(
                        null,
                        currentUrlPrefix,
                        item.Type,
                        DatabaseFileUtility.IMAGE_TYPE.TYPE,
                        seasonID,
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
