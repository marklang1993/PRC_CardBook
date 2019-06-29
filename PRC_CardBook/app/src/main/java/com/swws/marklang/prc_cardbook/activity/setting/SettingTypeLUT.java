package com.swws.marklang.prc_cardbook.activity.setting;

import java.util.HashMap;

// TODO: this class are not used currently
public class SettingTypeLUT {

    public enum Type {
        STRING,
        INTEGER,
        BOOLEAN
    }

    private static HashMap<String, Type> mLUT;

    /**
     * Init. the internal look-up table
     */
    private static void initialize() {
        mLUT = new HashMap<>();

        // Modify this LUT once the items in setting are changed
        mLUT.put("card_not_possessed_without_color", Type.BOOLEAN);
        mLUT.put("jr_card_display_by_number", Type.BOOLEAN);
    }

    private static Type getType(String key) {

        // If the LUT is null, initialize it first
        if (mLUT == null) {
            initialize();
        }

        if (!mLUT.containsKey(key)) return Type.STRING;

        return mLUT.get(key);
    }
}
