package com.swws.marklang.prc_cardbook.activity.statistics;

import android.os.AsyncTask;

import com.swws.marklang.prc_cardbook.activity.main.MainActivity;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryUtility;

public class CalculateTask extends AsyncTask<Void, Void, Void> {

    private SeriesItemAdapter mCaller;
    private int mPosition;

    private StringBuilder mResultString;

    public CalculateTask(SeriesItemAdapter caller, int position) {

        mCaller = caller;
        mPosition = position;

        mResultString = new StringBuilder();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Database database = MainActivity.getDatabaseByIndex(mPosition);
        int size = database.size();
        int countOfPossessedItem = 0;

        // Do calculation
        for (Item item: database) {
            int inventoryOfCurrentItem = InventoryUtility.getInventoryCount(database.seasonId(), item);
            if (inventoryOfCurrentItem > 0) {
                ++countOfPossessedItem;
            }
        }

        // Build the result string
        mResultString.append(countOfPossessedItem);
        mResultString.append("/");
        mResultString.append(size);
        mResultString.append("  ");
        if (size != countOfPossessedItem) {
            // Collection is incomplete
            mResultString.append("☆");

        } else {
            // Collection is complete
            mResultString.append("★");

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        super.onPostExecute(param);

        mCaller.notifyCalculationFinished(mPosition, mResultString.toString());
    }
}
