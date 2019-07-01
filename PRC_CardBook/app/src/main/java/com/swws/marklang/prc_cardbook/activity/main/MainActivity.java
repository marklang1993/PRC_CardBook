package com.swws.marklang.prc_cardbook.activity.main;

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
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.activity.about.AboutActivity;
import com.swws.marklang.prc_cardbook.activity.card.CardActivity;
import com.swws.marklang.prc_cardbook.activity.profile.ProfileActivity;
import com.swws.marklang.prc_cardbook.activity.qrcode.ScannerActivity;
import com.swws.marklang.prc_cardbook.activity.setting.SettingActivity;
import com.swws.marklang.prc_cardbook.activity.statistics.StatisticsActivity;
import com.swws.marklang.prc_cardbook.activity.system.SystemActivity;
import com.swws.marklang.prc_cardbook.activity.update.DatabaseUpdateActivity;
import com.swws.marklang.prc_cardbook.activity.profile.ProfileFileUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;
import com.swws.marklang.prc_cardbook.utility.database.Item;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private static ArrayList<Database> mDatabases;
    private static HashMap<String, Item> mItemIDLUT;

    private SeriesItemAdapter mSeriesItemAdapter;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init. UIs
        setTitle(getString(R.string.main_activity_name));
        setListView();
        initDrawer();
        initNavigationView();
        updateProfile();
    }

    /**
     * Set up "seriesListView"
     */
    private void setListView() {
        // Init. seriesListView
        ListView seriesListView = (ListView) findViewById(R.id.seriesListView);
        mSeriesItemAdapter = new SeriesItemAdapter(
                getApplicationContext(),
                mDatabases
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
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_main_activity);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close
        );
        drawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Init. Navigation View
     */
    private void initNavigationView() {
        mNavigationView = (NavigationView) findViewById(R.id.mainNavigationView);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                boolean result = true;

                switch (id)
                {
                    case R.id.profile_menu_item:
                        // Start "profile setting" activity
                        Intent profileSettingActivityIntent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivityForResult(profileSettingActivityIntent, Constants.REQUEST_PROFILE_UPDATE);
                        break;

                    case R.id.statistics_menu_item:
                        // Start "statistics" activity
                        Intent statisticsActivityIntent = new Intent(MainActivity.this, StatisticsActivity.class);
                        startActivity(statisticsActivityIntent);
                        break;

                    case R.id.qrcode_menu_item:
                        // Start QRCode Scanner
                        new IntentIntegrator(MainActivity.this).setCaptureActivity(ScannerActivity.class).initiateScan();
                        break;

                    case R.id.setting_menu_item:
                        // Start "setting" activity
                        Intent settingActivityIntent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(settingActivityIntent);
                        break;

                    case R.id.system_menu_item:
                        // Start "system" activity
                        Intent systemActivityIntent = new Intent(MainActivity.this, SystemActivity.class);
                        startActivityForResult(systemActivityIntent, Constants.REQUEST_SYSTEM_INVENTORY_DATABASE_CHANGE);
                        break;

                    case R.id.update_menu_item:
                        // Start the "local database update" activity
                        Intent databaseUpdateActivityIntent = new Intent(MainActivity.this, DatabaseUpdateActivity.class);
                        databaseUpdateActivityIntent.putExtra(DatabaseUpdateActivity.KEY_START_OPTION, 0); // "0" means it is started by user
                        startActivityForResult(databaseUpdateActivityIntent, Constants.REQUEST_UPDATE_RESULT_USER);
                        break;

                    case R.id.about_menu_item:
                        // Start the "about" activity
                        Intent aboutActivityIntent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(aboutActivityIntent);
                        break;

                    case R.id.exit_menu_item:
                        // Terminate this app
                        finishAndRemoveTask(); // MUST be used since API 21
                        break;

                    default:
                        // Unknown selection
                        result = false;
                        break;
                }

                // Shrink the navigation view
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main_activity);
                drawer.closeDrawer(GravityCompat.START);
                return result;
            }
        });

        // Init. onClickListener of profileImageView
        CircleImageView profileIconCircleImageView = (CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profileIconCircleImageView);
        profileIconCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start "profile setting" activity
                Intent profileSettingActivityIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivityForResult(profileSettingActivityIntent, Constants.REQUEST_PROFILE_UPDATE);
            }
        });
    }

    /**
     * Update profile display
     */
    private void updateProfile() {
        // Configure profile
        ProfileFileUtility profileFileUtility = ProfileFileUtility.getInstance();
        TextView profileNameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.profileNameTextView);
        CircleImageView profileIconCircleImageView = (CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profileIconCircleImageView);

        profileNameTextView.setText(profileFileUtility.getName());
        profileIconCircleImageView.setImageBitmap(profileFileUtility.getIcon());
    }

    /**
     * Item selection handler
     * @param menuItem
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(menuItem)) {
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Receive the Result from invoked Activities
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_UPDATE_RESULT_USER) {
           // Check is database updating successful
           if (resultCode == RESULT_OK) {
               // Start MainLoadActivity
               Intent startMainLoadActivityIntent = new Intent(
                       getApplicationContext(), MainLoadActivity.class
               );
               startMainLoadActivityIntent.putExtra(MainLoadActivity.KEY_IS_START_BY_MAIN_ACTIVITY, true);
               startActivityForResult(startMainLoadActivityIntent, Constants.REQUEST_LOAD_RESULT);
           }

        } else if (requestCode == Constants.REQUEST_LOAD_RESULT) {
            // Notify "seriesListView" to update
            mSeriesItemAdapter.notifyDataSetChanged(mDatabases);

        } else if (requestCode == Constants.REQUEST_PROFILE_UPDATE) {
            // Notify navigation drawer to update
            updateProfile();

        } else if (requestCode == Constants.REQUEST_SYSTEM_INVENTORY_DATABASE_CHANGE) {
            if (resultCode == RESULT_OK) {
                // Need to Start MainLoadActivity & update inventory database
                Intent startMainLoadActivityIntent = new Intent(
                        getApplicationContext(), MainLoadActivity.class
                );
                startMainLoadActivityIntent.putExtra(MainLoadActivity.KEY_IS_START_BY_MAIN_ACTIVITY, true);
                startActivity(startMainLoadActivityIntent);
            }
        }
    }

    /**
     * Get a Database entry by index
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

    /**
     * Set all Databases
     * @param newDatabases new Databases
     */
    public static void setAllDatabases(ArrayList<Database> newDatabases) { mDatabases = newDatabases; }

    /**
     * Get item id LUT
     * @return
     */
    public static HashMap<String, Item> getItemIDLUT() {
        return mItemIDLUT;
    }

    /**
     * Set item id LUT
     * @param lut
     */
    public static void setItemIDLUT(HashMap<String, Item> lut) {
        mItemIDLUT = lut;
    }
}
