package com.swws.marklang.prc_cardbook.activity.about.useragreement;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.swws.marklang.prc_cardbook.R;

import java.util.Locale;

public class UserAgreementActivity extends AppCompatActivity {

    // Constants
    private static final String mRepoLink = "https://github.com/marklang1993/PRC_CardBook";

    // Internal variables
    private String mLanguageCode;
    private UserAgreementFileUtility mUserAgreementFileUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_agreement);

        // Get language
        mLanguageCode = Locale.getDefault().getLanguage();

        // Get UserAgreementFileUtility
        mUserAgreementFileUtility = UserAgreementFileUtility.getInstance();

        // Init. UIs
        setTitle(R.string.about_user_agreement);
        initTextViews();
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
     * Init. all textviews for displaying user agreement
     */
    public void initTextViews() {
        String enVersion = mUserAgreementFileUtility.readUserAgreement("en");
        TextView userAgreementContent1TextView = findViewById(R.id.userAgreementContent1TextView);
        TextView userAgreementContent2TextView = findViewById(R.id.userAgreementContent2TextView);
        TextView userAgreementRepoTextView = findViewById(R.id.userAgreementRepoTextView);

        if (mLanguageCode.equals("ja")) {
            // Display Japanese version and English version
            String jaVersion = mUserAgreementFileUtility.readUserAgreement("ja");
            userAgreementContent1TextView.setText(jaVersion);
            userAgreementContent2TextView.setText(enVersion);

        } else if (mLanguageCode.equals("zh")) {
            // Display Chinese version and English version
            String zhVersion = mUserAgreementFileUtility.readUserAgreement("zh");
            userAgreementContent1TextView.setText(zhVersion);
            userAgreementContent2TextView.setText(enVersion);

        } else {
            // Only display English version
            userAgreementContent1TextView.setText(enVersion);
            userAgreementContent2TextView.setVisibility(View.INVISIBLE);
        }

        // Set RepoTextView
        userAgreementRepoTextView.setText(mRepoLink);
        userAgreementRepoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mRepoLink));
                startActivity(intent);
            }
        });
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
