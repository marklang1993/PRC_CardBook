package com.swws.marklang.prc_cardbook.activity.update;

import android.util.Log;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.HttpUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class DatabaseUpdater1 extends DatabaseUpdaterBase implements IDatabaseUpdater {


    public DatabaseUpdater1(DatabaseUpdateDownloadTask downloadTask) {
        super(downloadTask);
    }

    @Override
    public LinkedHashMap<String, String> GetUrlDict(ArrayList<Database> oldDatabases)
        throws HttpUtility.ServerErrorException, IOException {

        // Update the initial value of the progress bar
        mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(mProgressValues[0],
                mContext.getString(R.string.info_download_pages_list))
        );

        // Retrieve the raw dictionary from the official website
        LinkedHashMap<String, String> rawUrlDict = mHttpUtility.GetUrlDict(mIsPrintDebug);

        /**
         * Iterate all entries in the "oldDatabases" and check
         * (Weak) Assert: this entry is in the rawUrlDict
         * NOTE: Even though this entry is not in the rawUrlDict, the following
         *       operations will not be affected. That's why I use "If" here.
         *
         * Algorithm:
         * If this entry has the same item size as the entry in the rawUrlDict,
         * then remove this entry from the rawUrlDict.
         */
        Iterator<Database> oldDatabasesIterator = oldDatabases.iterator();
        while (oldDatabasesIterator.hasNext()) {
            Database oldEntry = oldDatabasesIterator.next();
            String correspondingUrl = oldEntry.url();

            if (rawUrlDict.containsKey(correspondingUrl)) { // This entry is in the rawUrlDict
                // Get the up-to-date item size of this entry
                LinkedList<String> allItemSubpageUrls = mHttpUtility.GetAllItemSubpageUrls(correspondingUrl);
                int newSize = allItemSubpageUrls.size();
                int oldSize = oldEntry.size();
                if (newSize == oldSize) { // This entry does not change at all
                    // Remove this entry from the update list
                    rawUrlDict.remove(correspondingUrl);
                }
            }
        }

        return rawUrlDict;
    }

    @Override
    public LinkedList<Database> GetDatabaseLinkedList(LinkedHashMap<String, String> urlDict)
            throws HttpUtility.ServerErrorException, IOException {

        // Update the initial value of the progress bar
        mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(mProgressValues[1],
                mContext.getString(R.string.info_download_coordinates))
        );

        // Retrieve each database from website based on "urlDict"
        LinkedList<Database> databases = new LinkedList<>();
        int countDatabases = urlDict.size();
        Iterator<Map.Entry<String, String>> iterator = urlDict.entrySet().iterator();
        for (int i = 0; i < countDatabases; ++i)
        {
            // ## Check whether this task is cancelled.
            if (mDownLoadTask.GetCancelStatus()) {
                return null;
            }

            // Get the current entry for retrieving the url of the series page.
            Map.Entry<String, String> urlEntry = iterator.next();
            String seriesName = urlEntry.getValue();
            String seriesRelativeUrl = urlEntry.getKey();

            // Update the value of the progress bar
            int currentProgress = calculateCurrentProgressValue(
                    i, 0, countDatabases, mProgressValues[1], mProgressValues[2] - 1
            );
            mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(currentProgress,
                    mContext.getString(R.string.info_download_coordinates) + " - " + seriesName
                    )
            );

            // Get the urls of the subpage of all items
            LinkedList<String> itemSubpageUrls = mHttpUtility.GetAllItemSubpageUrls(seriesRelativeUrl);

            // Generate the Database for the current series
            Database dataBase = new Database(seriesName, seriesRelativeUrl, SeasonID.SEASON_1ST);
            for (String itemSubpageUrl: itemSubpageUrls) {
                // ## Check whether this task is cancelled.
                if (mDownLoadTask.GetCancelStatus()) {
                    return null;
                }

                // Get 1 item of this series
                Item item = mHttpUtility.PopulateItems(seriesRelativeUrl, itemSubpageUrl, mIsPrintDebug);
                dataBase.Insert(item);
            }

            // Add this Database
            databases.add(dataBase);
        }
        return databases;
    }

    @Override
    public boolean GetItemImages(LinkedList<Database> databases)
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
            // Populate all items
            int cursorDataBase = 0;
            for (Item item: database) {

                // ## Check whether this task is cancelled.
                if (mDownLoadTask.GetCancelStatus()) {
                    return false;
                }

                // Update the value of the progress bar
                int currentProgress = calculateCurrentProgressValue(
                        cursorItem, 0, totalCount, mProgressValues[2], mProgressValues[3] - 1
                );
                mDownLoadTask.PublishProgress(mDownLoadTask.GetProgressMsg(currentProgress,
                        mContext.getString(R.string.info_download_coordinate_images) + "\n" + item.ItemName
                        )
                );

                // Download ItemImage
                mFileUtility.DownloadImage(
                        allImageNames, item.ItemImage, FileUtility.IMAGE_TYPE.IMAGE, SeasonID.SEASON_1ST, mHttpUtility, mIsPrintDebug);
                // Download BrandImage
                mFileUtility.DownloadImage(
                        null, item.Brand, FileUtility.IMAGE_TYPE.BRAND, SeasonID.SEASON_1ST, mHttpUtility, mIsPrintDebug);
                // Download TypeImage
                mFileUtility.DownloadImage(
                        null, item.Type, FileUtility.IMAGE_TYPE.TYPE, SeasonID.SEASON_1ST, mHttpUtility, mIsPrintDebug);

                // Print Procedure
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

        Log.e(this.getClass().getSimpleName(), String.format("Total Images Count: %d", allImageNames.size()));
        return true;
    }
}
