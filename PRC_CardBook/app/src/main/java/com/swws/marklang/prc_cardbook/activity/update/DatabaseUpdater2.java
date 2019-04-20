package com.swws.marklang.prc_cardbook.activity.update;

import com.swws.marklang.prc_cardbook.utility.HttpUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class DatabaseUpdater2 extends DatabaseUpdaterBase implements IDatabaseUpdater {
    public DatabaseUpdater2(DatabaseUpdateDownloadTask downloadTask) {
        super(downloadTask);
    }

    @Override
    public LinkedHashMap<String, String> GetUrlDict(ArrayList<Database> oldDatabases) throws HttpUtility.ServerErrorException, IOException {
        return null;
    }

    @Override
    public LinkedList<Database> GetDatabaseLinkedList(LinkedHashMap<String, String> urlDict) throws HttpUtility.ServerErrorException, IOException {
        return null;
    }

    @Override
    public boolean GetItemImages(LinkedList<Database> databases) throws HttpUtility.DirCreateException, IOException {
        return false;
    }
}
