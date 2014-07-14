package net.talldave.hibernatemetrics


enum DatabaseMetricsType {

    //ALL, QUERIES, DOMAINS, QUERY_CACHE, SECOND_LEVEL_CACHE, SESSIONS, MISC_COUNTS

    ALL('All'),
    TIME('ms'),
    COUNTS('Counts'),
    SQL('SQL'),
    DOMAINS('Domains'),
    QUERY_CACHE('Query Cache'),
    SECOND_LEVEL_CACHE('2L Cache'),
    SESSIONS('Sessions')

    private String displayVal

    private DatabaseMetricsType(String val) {
        this.displayVal = val
    }

    String toString() {
        displayVal
    }

}