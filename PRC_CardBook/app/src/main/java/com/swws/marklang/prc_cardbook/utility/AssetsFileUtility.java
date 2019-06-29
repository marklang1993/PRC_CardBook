package com.swws.marklang.prc_cardbook.utility;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;

public abstract class AssetsFileUtility extends FileUtility {

    private AssetManager mAssetManager;

    /**
     * Constructor
     */
    protected AssetsFileUtility() {
        mAssetManager = mApplicationContext.getAssets();
    }

    @Override
    protected BufferedReader getReader(String fileName) {
        try
        {

            InputStreamReader inputStreamReader = new InputStreamReader(
                    mAssetManager.open(getRootPath() + "/" + fileName)
            );
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            return bufferedReader;
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected boolean isFilePresented(String filePath) {
        BufferedReader reader = getReader(filePath);
        boolean result;
        if (reader == null) {
            result = false;

        } else {
            result = true;
            close(reader);
        }

        return result;
    }

    /**
     * It is impossible to write file in Assets
     * @param
     * @return
     */
    @Override
    protected BufferedWriter getWriter(String fileName) {
        throw new UnsupportedOperationException();
    }

    /**
     * It is impossible to write file in Assets
     * @param
     * @return
     */
    @Override
    protected void writeLine(BufferedWriter writer, String line, Boolean isPrint) {
        throw new UnsupportedOperationException();
    }

    /**
     * It is impossible to write file in Assets
     * @param
     * @return
     */
    @Override
    protected boolean checkAndMakeDirectory(String dirName) {
        throw new UnsupportedOperationException();
    }

}
