package com.swws.marklang.prc_cardbook.activity.setting;

import com.swws.marklang.prc_cardbook.utility.InternalFileUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class SettingFileUtility extends InternalFileUtility {

    // Singleton instance
    private static SettingFileUtility mSettingFileUtility = null;

    // Constants
    private static final boolean SETTING_DEBUG_IS_PRINT = false;
    private static final String SETTING_FILE = "setting.config";

    // Internal variables
    private HashMap<String, String> mSettings;

    /**
     * Constructor
     */
    private SettingFileUtility() {
        // Init. the dictionary for holding all setting items
        mSettings = new HashMap<>();

        // Load setting
        load();
    }

    /**
     * Get an instance of SettingFileUtility
     * @return
     */
    public static SettingFileUtility getInstance() {
        // TODO: thread-safe?
        if (mSettingFileUtility == null) {
            mSettingFileUtility = new SettingFileUtility();
        }
        return mSettingFileUtility;
    }

    /**
     * Write an item of app setting
     * @param key
     * @param value
     */
    public void writeItem(String key, String value) {
        if (mSettings.containsKey(key)) {
            // Modify the existed setting item
            mSettings.remove(key);
            mSettings.put(key, value);

        } else {
            // Create an new setting item
            mSettings.put(key, value);
        }
    }

    /**
     * Read an item of app setting
     * @param key
     * @return null if such key does not exist
     */
    public String readItem(String key) {

        if (mSettings.containsKey(key)) {
            return mSettings.get(key);
        }
        return null;
    }

    /**
     * Load all setting items to the memory
     * @return false if setting file does not exist OR error
     */
    private boolean load() {
        BufferedReader bufferedReader = getReader(SETTING_FILE);

        if (bufferedReader != null) {
            // Get count
            String itemCountString = readLine(bufferedReader, SETTING_DEBUG_IS_PRINT);
            int itemCount = Integer.valueOf(itemCountString);
            while (itemCount > 0) {
                String line = readLine(bufferedReader, SETTING_DEBUG_IS_PRINT);
                int indexOfSeparator = line.indexOf(',');
                if (indexOfSeparator > 0 && indexOfSeparator < (line.length() - 1)) {
                    // Get key-value pair
                    String key = line.substring(0, indexOfSeparator);
                    String value = line.substring(indexOfSeparator + 1);
                    // Put this pair in the dictionary
                    mSettings.put(key, value);
                }
                --itemCount;
            }
            close(bufferedReader);
            return true;
        }

        return false;
    }

    /**
     * Save all setting items in memory to the file
     * @return false if the setting file cannot be written
     */
    public boolean save() {
        BufferedWriter bufferedWriter = getWriter(SETTING_FILE);

        if (bufferedWriter != null) {
            // Save count
            int count = mSettings.size();
            writeLine(bufferedWriter, String.valueOf(count), SETTING_DEBUG_IS_PRINT);

            // Save each item
            Set<Map.Entry<String, String>> entrySet = mSettings.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                StringBuilder entryStringBuilder = new StringBuilder();
                entryStringBuilder.append(entry.getKey());
                entryStringBuilder.append(',');
                entryStringBuilder.append(entry.getValue());

                writeLine(bufferedWriter, entryStringBuilder.toString(), SETTING_DEBUG_IS_PRINT);
            }

            close(bufferedWriter);
            return true;
        }

        return false;
    }

    /**
     * Get boolean value by given string
     * TODO: replace this function with a more general one by using SettingTypeLUT
     * @param value
     * @return
     */
    public boolean getBooleanValue(String value) {
        if (value == null) return false;
        return value.equals("true");
    }

    /**
     * Generate string of the value of given boolean
     * TODO: replace this function with a more general one by using SettingTypeLUT
     * @param value
     * @return
     */
    public String putBooleanValue(boolean value) {
        return value ? "true" : "false";
    }
}
