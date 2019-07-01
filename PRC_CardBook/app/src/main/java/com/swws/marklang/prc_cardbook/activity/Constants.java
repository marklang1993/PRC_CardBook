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

    // Used to start "SystemProgressActivity"
    public static final int REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_EXPORTING = 7;
    public static final int REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_IMPORTING = 8;
    public static final int REQUEST_SYSTEM_PROGRESS_INVENTORY_DB_CLEAR = 9;

    // Used to start "CardDetailActivity"
    public static final int REQUEST_CARD_INVENTORY_CHANGE = 10;      // Used by CardActivity
    public static final int REQUEST_CARD_DETAIL_DISPLAY_DONE = 11;  // Used by ScannerActivity

    // Used to start "SystemActivity"
    public static final int REQUEST_SYSTEM_INVENTORY_DATABASE_CHANGE = 12;


    /**
     * requestCode definition for requestPermissions()
     */
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 100;


    // Others
    public static final boolean READ_ALL_METADATA_DEBUG = false;
}
