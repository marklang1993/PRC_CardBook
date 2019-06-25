package com.swws.marklang.prc_cardbook.activity;

public class Constants {

    /**
     * requestCode definition for startActivityForResult()
     */
    // Used to start "DatabaseUpdateActivity"
    public static final int REQUEST_UPDATE_RESULT_USER = 1;
    public static final int REQUEST_UPDATE_RESULT_APP = 2;
    // Used to start "MainLoadActivity"
    public static final int REQUEST_LOAD_RESULT = 3;
    // Used to start "ProfileActivity"
    public static final int REQUEST_PROFILE_UPDATE = 4;
    // Used to start Image File Explorer
    public static final int REQUEST_IMAGE_FILE_SELECTION = 5;
    // Used to start "ImageCropActivity"
    public static final int REQUEST_CROP_IMAGE = 6;


    /**
     * requestCode definition for requestPermissions()
     */
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 100;


    // Others
    public static final boolean READ_ALL_METADATA_DEBUG = false;
}
