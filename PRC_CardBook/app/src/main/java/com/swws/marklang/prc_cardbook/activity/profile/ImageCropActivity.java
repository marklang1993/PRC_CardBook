package com.swws.marklang.prc_cardbook.activity.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.Constants;
import com.swws.marklang.prc_cardbook.activity.main.MainLoadActivity;
import com.swws.marklang.prc_cardbook.utility.ProfileFileUtility;

public class ImageCropActivity extends AppCompatActivity {

    // Internal variables
    private CropImageView mCropImageView;
    private RectF mFrameRect = null;
    private Uri mSourceUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        // Configure mCropImageView
        mCropImageView = findViewById(R.id.cropImageView);
        mCropImageView.setCropMode(CropImageView.CropMode.CIRCLE_SQUARE);
        mCropImageView.setCompressFormat(Bitmap.CompressFormat.PNG);
        mCropImageView.setCompressQuality(100);
        mCropImageView.setOutputMaxSize(200, 200);

        // Set UI
        setTitle(R.string.image_crop_activity_name);
        initButtons();

        // Start this activity from picking an image
        startImagePicker();
    }

    /**
     * Init. all buttons in this activity
     */
    private void initButtons(){
        // Image Pick Button
        ImageButton cropImagePickButton = (ImageButton) findViewById(R.id.cropImagePickButton);
        cropImagePickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImagePicker();
            }
        });

        // Confirm Button
        ImageButton cropImageConfirmButton = (ImageButton) findViewById(R.id.cropImageConfirmButton);
        cropImageConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSourceUri != null) {
                    // Crop image
                    mCropImageView.crop(mSourceUri).execute(new CropCallback() {
                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(ImageCropActivity.this, R.string.exception_profile_crop_icon_fail, Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onSuccess(Bitmap cropped) {
                            // Save the cropped image
                            ProfileFileUtility profileFileUtility = ProfileFileUtility.getInstance();
                            profileFileUtility.setIcon(cropped);
                            // Set return result & close this activity
                            setResult(RESULT_OK);
                            finish();
                        }
                    });

                } else {
                    // Not image is selected - do nothing and close this activity
                    finish();
                }
            }
        });

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display this button: Enable
    }

    /**
     * Start file explorer to pick an image
     */
    private void startImagePicker() {
        // Check the permission in run-time.
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // The required permission is granted - Start image picker
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setType("image/*");
            startActivityForResult(intent, Constants.REQUEST_IMAGE_FILE_SELECTION);

        } else {
            // The required permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
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

    /**
     * Get result from image picker
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_IMAGE_FILE_SELECTION) {
            if (resultCode == Activity.RESULT_OK) {
                // Get URI and grant persistable permission
                Context applicationContext = MainLoadActivity.getCurrentApplicationContext();
                mSourceUri = data.getData();
                applicationContext.grantUriPermission(
                        applicationContext.getPackageName(),
                        mSourceUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                // Load image
                mFrameRect = null;  // Reset frame rect
                mCropImageView.load(mSourceUri)
                        .initialFrameRect(mFrameRect)
                        .useThumbnail(true)
                        .execute(new LoadCallback(){
                            @Override public void onSuccess() {
                            }

                            @Override public void onError(Throwable e) {
                                mSourceUri = null;
                            }
                        });
            }
        }
    }

    /**
     * Get result of requesting permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        if (requestCode == Constants.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted by the user - try to start image picker again
                startImagePicker();

            } else {
                // Permission is NOT granted by the user - show an error message
                Toast.makeText(this, R.string.exception_no_permission_runtime, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}
