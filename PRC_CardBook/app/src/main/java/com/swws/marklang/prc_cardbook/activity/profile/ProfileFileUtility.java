package com.swws.marklang.prc_cardbook.activity.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.swws.marklang.prc_cardbook.R;
import com.swws.marklang.prc_cardbook.activity.main.MainLoadActivity;
import com.swws.marklang.prc_cardbook.utility.InternalFileUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;

public final class ProfileFileUtility extends InternalFileUtility {

    // Singleton instance
    private static ProfileFileUtility mProfileFileUtility = null;
    private static Context mApplicationContext = null;

    // Constants
    public static final int PROFILE_NAME_SIZE = 10;
    private static final boolean PROFILE_DEBUG_IS_PRINT = false;

    private static final String PROFILE_FILE = "profile.config";
    private static final String PROFILE_ICON = "profile_icon.png";
    private static final String PROFILE_ICON_DEFAULT = "default";
    private static final String PROFILE_ICON_SELF_DEFINED = "self-defined";

    // Internal variables
    private String mName;
    private boolean mIsUserDefinedIcon;


    /**
     * Constructor
     */
    private ProfileFileUtility() {
        // Get application context
        mApplicationContext = MainLoadActivity.getCurrentApplicationContext();

        // Load profile
        load();
    }

    /**
     * Get an instance of ProfileFileUtility
     * @return
     */
    public static ProfileFileUtility getInstance() {
        // TODO: thread-safe?
        if (mProfileFileUtility == null) {
            mProfileFileUtility = new ProfileFileUtility();
        }
        return mProfileFileUtility;
    }

    /**
     * Get user name
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * Get icon bitmap
     * @return
     */
    public Bitmap getIcon() {
        Bitmap iconBitmap;

        if (mIsUserDefinedIcon) {
            // User defined icon
            String iconAbsolutePath = getInternalPath() + "/" + PROFILE_ICON;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            iconBitmap = BitmapFactory.decodeFile(iconAbsolutePath);

        } else {
            // Default icon
            iconBitmap = BitmapFactory.decodeResource(
                    mApplicationContext.getResources(),
                    R.drawable.ic_default_character);
        }

        return iconBitmap;
    }

    /**
     * Set new name
     * @param name
     * @return false if the length of new name is longer than PROFILE_NAME_SIZE
     */
    public boolean setName(String name) {
        if (name.length() > PROFILE_NAME_SIZE) {
            // New name is too long
            return false;
        }

        mName = name;
        return true;
    }

    /**
     * Set new icon
     * @param icon
     */
    public void setIcon(Bitmap icon) {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getInternalPath() + "/" + PROFILE_ICON);
            icon.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();

        } catch (Exception ex) {
            ;
        }

        mIsUserDefinedIcon = true;
    }

    /**
     * Load profile from file
     */
    private void load() {
        // Detect profile config file
        if (isFilePresented(PROFILE_FILE)) {
            // Profile file is presented
            BufferedReader bufferedReader = getReader(PROFILE_FILE);

            try {
                // Read Profile
                mName = readLine(bufferedReader, PROFILE_DEBUG_IS_PRINT);
                String strIsDefaultIcon = readLine(bufferedReader, PROFILE_DEBUG_IS_PRINT);
                mIsUserDefinedIcon = strIsDefaultIcon.equals(PROFILE_ICON_SELF_DEFINED);

                close(bufferedReader);
                return;

            } catch (Exception ex) {
                // Any exception occurred
            }
        }

        // Profile file is NOT presented / exception occurred - generate the default setting
        mName = mApplicationContext.getString(R.string.profile_default_name);
        mIsUserDefinedIcon = false;
    }

    /**
     * Save profile to file
     * @return
     */
    public boolean save() {
        BufferedWriter bufferedWriter = getWriter(PROFILE_FILE);

        try {
            // Write Profile
            writeLine(bufferedWriter, mName, PROFILE_DEBUG_IS_PRINT);
            if (mIsUserDefinedIcon) {
                writeLine(bufferedWriter, PROFILE_ICON_SELF_DEFINED, PROFILE_DEBUG_IS_PRINT);

            } else {
                writeLine(bufferedWriter, PROFILE_ICON_DEFAULT, PROFILE_DEBUG_IS_PRINT);
            }

            close(bufferedWriter);

        } catch (Exception ex) {
            // Any exception occurred
            return false;
        }

        return true;
    }
}
