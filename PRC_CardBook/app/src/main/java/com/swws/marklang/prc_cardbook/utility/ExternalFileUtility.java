package com.swws.marklang.prc_cardbook.utility;

import android.os.Environment;

import com.swws.marklang.prc_cardbook.activity.main.MainLoadActivity;

import java.io.File;


public class ExternalFileUtility extends FileUtility {

    /**
     * Constructor
     */
    protected ExternalFileUtility() {
        super();
    }

    /**
     * Initialize the path of the External Storage used by this app
     * @return "null" once error occurs otherwise return the path
     */
    @Override
    protected String initRootDirectory() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String packageName = MainLoadActivity.getCurrentApplicationContext().getPackageName();
        String path = externalStorageDirectory.getPath() + "/" + packageName;

        // Check and ensure the external storage path is valid
        File appExternalStoragePath = new File(path);
        if (!appExternalStoragePath.exists()) {
            if (!appExternalStoragePath.mkdirs()) return null;
        }

        return path;
    }

    /**
     * Get the path of external storage
     * @return
     */
    protected String getExternallPath() {
        return getRootPath();
    }
}
