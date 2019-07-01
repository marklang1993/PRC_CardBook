package com.swws.marklang.prc_cardbook.activity.system.inventory;

import com.swws.marklang.prc_cardbook.activity.system.SystemProgressActivity;


abstract class ExternalFileOperationInventoryDatabaseTaskBase extends InventoryDatabaseOperationTaskBase {

    protected String mFileName;

    ExternalFileOperationInventoryDatabaseTaskBase(
            SystemProgressActivity progressDisplayActivity,
            String fileName
    ) {
        super(progressDisplayActivity);
        mFileName = fileName;
    }
}
