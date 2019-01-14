package com.swws.marklang.prc_cardbook.activity.main;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.card.CardActivity;
import com.swws.marklang.prc_cardbook.activity.qrcode.ScannerActivity;
import com.swws.marklang.prc_cardbook.activity.update.DatabaseUpdateActivity;
import com.swws.marklang.prc_cardbook.utility.FileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;
import com.swws.marklang.prc_cardbook.utility.inventory.Inventory;
import com.swws.marklang.prc_cardbook.utility.inventory.InventoryDatabase;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static ArrayList<Database> mDatabases = null;
    public static InventoryDatabase mInventoryDB = null;

    // Used for "startActivityForResult()"
    private static final int REQUEST_UPDATE_RESULT_USER = 1;
    private static final int REQUEST_UPDATE_RESULT_APP = 2;

    private FileUtility mFileUtility;
    private SeriesItemAdapter mSeriesItemAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check is metadata presented
        mFileUtility = new FileUtility(getApplicationContext());
        if (!mFileUtility.IsMetadataFilePresented()) {
            // Forcely start "local database update" activity
            Intent databaseUpdateActivityIntent = new Intent(getApplicationContext(), DatabaseUpdateActivity.class);
            databaseUpdateActivityIntent.putExtra(DatabaseUpdateActivity.KEY_START_OPTION, 1); // "1" means it is started by app.
            startActivityForResult(databaseUpdateActivityIntent, REQUEST_UPDATE_RESULT_APP);

        } else {
            // Init. data
            initData();
        }

        // Init. UIs
        setListView();
        initDrawer();
        initNavigationView();
    }

    /**
     * Init. MetaData and InventoryDB
     */
    private void initData() {
        // Read meta data
        if (mDatabases == null)
        {
            mDatabases = mFileUtility.ReadAllMetaData(false);
        }

        // Get access to Inventory DB
        if (mInventoryDB == null)
        {
            // TODO: use thread-safe way to create this singleton
            // TODO: use background thread to access DB --- remove allowMainThreadQueries()
            mInventoryDB = Room.databaseBuilder(getApplicationContext(),
                    InventoryDatabase.class,
                    getString(R.string.inventory_db_file_name)).allowMainThreadQueries().build();
        }

        // Init. the content of Inventory DB
        for (Database database: mDatabases) {
            for (Item item: database) {
                // Check is this item registered in the inventory map
                String itemImageId = item.getImageID();
                Inventory[] inventories = mInventoryDB.inventoryDAO().queryInventoryByItemID(itemImageId);
                if (inventories.length == 0)
                {
                    // If not, insert a new record
                    Inventory newInventory = new Inventory();
                    newInventory.mInventoryItemID = itemImageId;
                    newInventory.mInventoryItemCount = 0;
                    mInventoryDB.inventoryDAO().insertInventory(newInventory);
                }
            }
        }
    }

    /**
     * Set up "seriesListView"
     */
    private void setListView() {
        // Init. seriesListView
        ListView seriesListView = (ListView) findViewById(R.id.seriesListView);
        mSeriesItemAdapter = new SeriesItemAdapter(
                getApplicationContext(), mDatabases
        );
        seriesListView.setAdapter(mSeriesItemAdapter);
        seriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showCardActivity = new Intent(getApplicationContext(), CardActivity.class);
                // Pass params to "CardActivity"
                showCardActivity.putExtra(CardActivity.KEY_SERIES_INDEX, position);
                // Start
                startActivity(showCardActivity);
            }
        });
    }

    /**
     * Init. Drawer
     */
    private void initDrawer() {
        // Init. DrawerLayout and DrawerToggle
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_main_activity);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.open,
                R.string.close
        );
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Init. Navigation View
     */
    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.mainNavigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                boolean result;

                switch (id)
                {
                    case R.id.qrcode_menu_item:
                        result = true;
                        // Start QRCode Scanner
                        Intent scannerActivityIntent = new Intent(getApplicationContext(), ScannerActivity.class);
                        startActivity(scannerActivityIntent);
                        break;

                    case R.id.setting_menu_item:
                        result = true;
                        break;

                    case R.id.update_menu_item:
                        result = true;
                        // Start the "local database update" activity
                        Intent databaseUpdateActivityIntent = new Intent(getApplicationContext(), DatabaseUpdateActivity.class);
                        databaseUpdateActivityIntent.putExtra(DatabaseUpdateActivity.KEY_START_OPTION, 0); // "0" means it is started by user
                        startActivityForResult(databaseUpdateActivityIntent, REQUEST_UPDATE_RESULT_USER);
                        break;

                    case R.id.exit_menu_item:
                        result = true;
                        finishAndRemoveTask(); // MUST be used since API 21
                        break;

                    default:
                        result = false;
                        break;
                }

                // Shrink the navigation view
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout_main_activity);
                drawer.closeDrawer(GravityCompat.START);
                return result;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(menuItem)) {
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Notify "seriesListView" to update once the database updating is finished.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

       switch (requestCode) {
           case REQUEST_UPDATE_RESULT_USER:
               // Check is database updating successful
               if (resultCode == RESULT_OK) {
                   // Reload database
                   mDatabases = null;
                   initData();
                   // Update seriesListView
                   mSeriesItemAdapter.notifyDataSetChanged(mDatabases);
               }
            break;

           case REQUEST_UPDATE_RESULT_APP:
               // Check is database updating successful
               if (resultCode == RESULT_OK) {
                   // Reload database
                   mDatabases = null;
                   initData();
                   // Update seriesListView
                   mSeriesItemAdapter.notifyDataSetChanged(mDatabases);

               } else {
                   // Update failed - Terminate the app.
                   finishAndRemoveTask(); // MUST be used since API 21
               }
        }
    }

    /**
     * Get a single Database by index
     * @param databaseIndex
     */
    public static Database getDatabaseByIndex(int databaseIndex) {
        return mDatabases.get(databaseIndex);
    }

    /**
     * Get all Databases
     * @return
     */
    public static ArrayList<Database> getAllDatabases() {
        return mDatabases;
    }
}
