package com.swws.marklang.prc_cardbook.activity.profile;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.utility.ProfileFileUtility;

public class ProfileActivity extends AppCompatActivity {

    // Profile File Utility
    private ProfileFileUtility mProfileFileUtility;

    // Views
    private ImageView mProfileIconImageView;
    private EditText mProfileNameEditText;

    // Icon cache
    private Bitmap mIconBitmapCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Init. Profile File Utility
        mProfileFileUtility = ProfileFileUtility.getInstance();

        // Find out views
        mProfileIconImageView = (ImageView) findViewById(R.id.profileIconImageView);
        mProfileNameEditText = (EditText) findViewById(R.id.profileNameEditText);

        // Init. UI
        setTitle(R.string.profile_activity_name);
        initValues();
        initButtons();
    }

    /**
     * Set initial values in all views
     */
    public void initValues() {
        mIconBitmapCache = mProfileFileUtility.getIcon();
        mProfileIconImageView.setImageBitmap(mIconBitmapCache);

        mProfileNameEditText.setText(mProfileFileUtility.getName());
    }

    public void initButtons() {
        // Save button
        Button saveButton = (Button) findViewById(R.id.profileSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set new name
                String newName = mProfileNameEditText.getText().toString();
                if (!mProfileFileUtility.setName(newName)) {
                    // Name is too long
                    Toast.makeText(
                            ProfileActivity.this,
                            String.format(
                                    ProfileActivity.this.getString(R.string.exception_profile_name_too_long),
                                    ProfileFileUtility.PROFILE_NAME_SIZE
                            ),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Set new icon
                mProfileFileUtility.setIcon(mIconBitmapCache);

                // Save
                if (!mProfileFileUtility.save()) {
                    // Fail to save
                    Toast.makeText(ProfileActivity.this, R.string.exception_profile_save_fail, Toast.LENGTH_LONG).show();
                } else {
                    // Saving completed
                    Toast.makeText(ProfileActivity.this, R.string.info_profile_saved, Toast.LENGTH_SHORT).show();
                    finish(); // Close this activity
                }
            }
        });

        // Cancel button
        Button cancelButton = (Button) findViewById(R.id.profileCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity
                finish();
            }
        });

        // Icon Button
        mProfileIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

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
