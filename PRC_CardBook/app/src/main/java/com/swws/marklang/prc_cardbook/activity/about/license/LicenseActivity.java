package com.swws.marklang.prc_cardbook.activity.about.license;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.R;

import java.util.ArrayList;


public class LicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        // Init. UIs
        setTitle(R.string.about_license_info);
        setListView();
        initButtons();
    }

    /**
     * Set up "aboutListView"
     */
    private void setListView() {
        // Read license assets
        LicenseFileUtility licenseFileUtility = LicenseFileUtility.getInstance();
        ArrayList<LicenseItem> licenseItems = licenseFileUtility.getList();

        // Init. licenseListView
        ListView licenseListView = (ListView) findViewById(R.id.licenseListView);
        LicenseItemAdapter licenseItemAdapter = new LicenseItemAdapter(licenseItems);
        licenseListView.setAdapter(licenseItemAdapter);
    }

    /**
     * Init. all buttons in this activity
     */
    public void initButtons() {
        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
    }

    /**
     * Close this activity
     * @return
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
