package com.swws.marklang.prc_cardbook.activity.about;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.swws.marklang.prc_cardbook.R;

public class UserAgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_agreement);

        // Init. UIs
        setTitle(R.string.about_user_agreement);
        initButtons();
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
