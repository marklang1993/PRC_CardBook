package com.swws.marklang.prc_cardbook.activity.about.useragreement;

import com.swws.marklang.prc_cardbook.activity.about.AboutFileUtility;

public class UserAgreementFileUtility extends AboutFileUtility {

    // Singleton
    private static UserAgreementFileUtility mUserAgreementFileUtility = null;

    // Constants
    private static final String ROOT_DIR = "license";

    /**
     * Constructor
     */
    private UserAgreementFileUtility() {
        super();
    }

    /**
     * Get an instance of UserAgreementFileUtility
     * @return
     */
    public static UserAgreementFileUtility getInstance() {
        // TODO: thread-safe?
        if (mUserAgreementFileUtility == null) {
            mUserAgreementFileUtility = new UserAgreementFileUtility();
        }
        return mUserAgreementFileUtility;
    }

    @Override
    protected String initRootDirectory() {
        return ROOT_DIR;
    }

    /**
     * Read user agreement content
     * @param languageCode
     * @return
     */
    public String readUserAgreement(String languageCode) {
        String fileName = languageCode + ".txt";
        return readLicenseContent(fileName);
    }
}
