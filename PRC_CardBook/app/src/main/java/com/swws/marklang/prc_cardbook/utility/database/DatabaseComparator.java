package com.swws.marklang.prc_cardbook.utility.database;

import java.util.Comparator;

public class DatabaseComparator implements Comparator<Database> {

    /**
     * Rule: The database with larger(newer) SeasonID should be preceded.
     * @param o1
     * @param o2
     * @return
     */

    @Override
    public int compare(Database o1, Database o2) {
        return -Integer.compare(o1.seasonId().ordinal(), o2.seasonId().ordinal());
    }
}
