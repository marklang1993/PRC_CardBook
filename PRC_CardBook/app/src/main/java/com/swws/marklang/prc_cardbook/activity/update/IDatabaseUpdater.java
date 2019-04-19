package com.swws.marklang.prc_cardbook.activity.update;

import com.swws.marklang.prc_cardbook.utility.HttpUtility;
import com.swws.marklang.prc_cardbook.utility.database.Database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public interface IDatabaseUpdater {

    /**
     * 1st Step: Get the dictionary of all URLs
     * @return
     * @throws HttpUtility.ServerErrorException
     * @throws IOException
     */
    LinkedHashMap<String, String> GetUrlDict(ArrayList<Database> oldDatabases)
            throws HttpUtility.ServerErrorException, IOException;

    /**
     * 2nd Step: Get all databases based on "urlDict"
     * @param urlDict
     * @return
     * @throws HttpUtility.ServerErrorException
     * @throws IOException
     */
    LinkedList<Database> GetDatabaseLinkedList(LinkedHashMap<String, String> urlDict)
            throws HttpUtility.ServerErrorException, IOException;

    /**
     * 3rd Step: Get all item images
     * @param databases
     * @return isFinished
     */
    boolean GetItemImages(LinkedList<Database> databases)
            throws HttpUtility.DirCreateException, IOException;
}
