package com.swws.marklang.prc_cardbook.utility;

public class InternalFileUtility extends FileUtility {

    /**
     * Constructor
     */
    protected InternalFileUtility() {
        super();
    }

    @Override
    protected String initRootDirectory() {
        return mApplicationContext.getFilesDir().getPath();
    }

    /**
     * Get the path of internal storage
     * @return
     */
    protected String getInternalPath() {
        return getRootPath();
    }
}
