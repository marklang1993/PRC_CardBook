package com.swws.marklang.prc_cardbook.activity;

public class Constants {

    /**
     * requestCode definition for startActivityForResult()
     */
    // Used to start "DatabaseUpdateActivity"
    public static final int REQUEST_AR_UPDATE_RESULT_USER = 1;
    public static final int REQUEST_AR_UPDATE_RESULT_APP = 2;

    // Used to start "MainLoadActivity"
    public static final int REQUEST_AR_LOAD_RESULT = 3;

    // Used to start "ProfileActivity"
    public static final int REQUEST_AR_PROFILE_UPDATE = 4;

    // Used to start Image File Explorer
    public static final int REQUEST_AR_IMAGE_FILE_SELECTION = 5;

    // Used to start "ImageCropActivity"
    public static final int REQUEST_AR_CROP_IMAGE = 6;

    // Used to start "SystemProgressActivity"
    public static final int REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_EXPORTING = 7;
    public static final int REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_IMPORTING = 8;
    public static final int REQUEST_AR_SYSTEM_PROGRESS_INVENTORY_DB_CLEAR = 9;

    // Used to start "CardDetailActivity"
    public static final int REQUEST_AR_CARD_INVENTORY_CHANGE = 10;      // Used by CardActivity
    public static final int REQUEST_AR_CARD_DETAIL_DISPLAY_DONE = 11;   // Used by ScannerActivity

    // Used to start "SystemActivity"
    public static final int REQUEST_AR_SYSTEM_INVENTORY_DATABASE_CHANGE = 12;


    /**
     * requestCode definition for requestPermissions()
     */
    public static final int REQUEST_PERMISSION_CAMERA = 100;
    public static final int REQUEST_PERMISSION_NETWORK = 101;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_IMG_CROP = 102;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE_IMPORT_INV_DB = 103;
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_EXPORT_INV_DB = 104;


    // Others
    public static final boolean READ_ALL_METADATA_DEBUG = false;
}
