package com.swws.marklang.prc_cardbook.activity.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // Profile File Utility
    private ProfileFileUtility mProfileFileUtility;

    // Views
    private CircleImageView mProfileIconCircleImageView;
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
        mProfileIconCircleImageView = (CircleImageView) findViewById(R.id.profileIconCircleImageView);
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
        mProfileIconCircleImageView.setImageBitmap(mIconBitmapCache);

        mProfileNameEditText.setText(mProfileFileUtility.getName());
    }

    /**
     * Init. all buttons in this activity
     */
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
        mProfileIconCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startImageCropActivityIntent = new Intent(ProfileActivity.this, ImageCropActivity.class);
                startActivityForResult(startImageCropActivityIntent, Constants.REQUEST_CROP_IMAGE);
            }
        });

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
    }

    /**
     * Get result from "ImageCropActivity"
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CROP_IMAGE) {
            if (resultCode == RESULT_OK) {
                // Update icon display
                mIconBitmapCache = mProfileFileUtility.getIcon();
                mProfileIconCircleImageView.setImageBitmap(mIconBitmapCache);
            }
        }
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
