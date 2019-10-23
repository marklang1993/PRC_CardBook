package com.swws.marklang.prc_cardbook.activity.search;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.database.ItemEx;
import com.swws.marklang.prc_cardbook.utility.database.SeasonID;

import java.util.ArrayList;
import java.util.LinkedList;

public class SearchTask extends AsyncTask<Void, Integer, Boolean> {

    private SearchActivity mSearchActivity;
    private ProgressBar mProgressBar;
    private boolean mIsCancelled;

    LinkedList<String> mTargetItemNames;
    private ArrayList<ItemEx> mResult;


    /**
     * Constructor
     * @param searchActivity
     */
    public SearchTask(SearchActivity searchActivity, LinkedList<String> targetItemNames) {
        mSearchActivity = searchActivity;
        mProgressBar = (ProgressBar) searchActivity.findViewById(R.id.searchProgressBar);
        mProgressBar.setMax(100);
        mIsCancelled = false;

        mTargetItemNames = targetItemNames;
        mResult = new ArrayList<>(100);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        ArrayList<Database> allDatabases = MainActivity.getAllDatabases();

        int currentIndex = 0;
        int countDatabase = allDatabases.size();

        // Iterate all databases
        for (Database database: allDatabases) {
            // Update progress bar
            publishProgress(currentIndex, countDatabase);

            // Check is operation cancelled
            if (mIsCancelled) {
                mResult.clear();
                return false;
            }

            // Iterate the current database
            SeasonID currentSeasonId = database.seasonId();
            for (Item item : database) {
                for (String targetItemName : mTargetItemNames) {
                    if (item.ItemName.contains(targetItemName)) {
                        // A match is found
                        ItemEx itemEx = new ItemEx(item, currentSeasonId);
                        mResult.add(itemEx);
                        break;
                    }
                }
            }

            ++currentIndex;
        }

        publishProgress(currentIndex, countDatabase);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        mProgressBar = null;
        if (aBoolean){
            // This task successfully completes
            mSearchActivity.notify(mResult);
        }
        mSearchActivity = null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        int currentIndex = values[0];
        int count = values[1];
        double progress = 100.d * currentIndex / count;

        mProgressBar.setProgress((int)progress);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mIsCancelled = true;
    }
}
