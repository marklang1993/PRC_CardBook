package com.swws.marklang.prc_cardbook.activity.about;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.about.license.LicenseActivity;
import com.swws.marklang.prc_cardbook.activity.about.useragreement.UserAgreementActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Init. UIs
        setTitle(R.string.about_activity_name);
        setListView();
        initButtons();
    }

    /**
     * Set up "aboutListView"
     */
    private void setListView() {
        // Init. aboutListView
        ListView aboutListView = (ListView) findViewById(R.id.aboutListView);
        AboutItemAdapter aboutItemAdapter = new AboutItemAdapter(getApplicationContext());
        aboutListView.setAdapter(aboutItemAdapter);
        aboutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 1: // User Agreement
                        intent = new Intent(getApplicationContext(), UserAgreementActivity.class);
                        break;

                    case 2: // License
                        intent = new Intent(getApplicationContext(), LicenseActivity.class);
                        break;
                }

                // Start Activity
                if (intent != null) startActivity(intent);
            }
        });
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
