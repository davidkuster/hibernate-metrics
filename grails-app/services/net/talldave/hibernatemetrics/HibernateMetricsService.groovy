package net.talldave.hibernatemetrics

import net.talldave.hibernatemetrics.util.SqlLogger
import net.talldave.hibernatemetrics.util.SqlFormatter
import static net.talldave.hibernatemetrics.DatabaseMetricsType.*

import grails.util.Holders
import org.hibernate.stat.Statistics


class HibernateMetricsService {

    static transactional = false

    def sessionFactory

    def config = Holders.config

    // holding state in a service - bad Dave, bad!
    boolean enabled = config.grails.plugins.hibernateMetrics.enabled ?: false
    boolean logToConsole = config.grails.plugins.hibernateMetrics.logSqlToConsole ?: true
    def excludeActions = [ 'hibernateMetrics':['*'] ] << config.grails.plugins.hibernateMetrics.excludeActions

    def initialized

    def startMillis
    def actionTime
    def totalTime
    def viewTime

    def sqlLogger



    boolean shouldTrack( controllerName, actionName ) {
        if ( ! enabled )
            return false
        else {
            def all = excludeActions['*']
            def controller = excludeActions[controllerName]
            if ( all?.contains( actionName ) || all?.contains('*')
                || controller?.contains( actionName ) || controller?.contains('*')  )
            {
                return false
            }
            else {
                if ( ! initialized ) enableMetrics()
                return true
            }
        }
        true
    }


    void enableMetrics() {
        sessionFactory.settings.sqlStatementLogger.logToStdout = true
        sessionFactory.settings.sqlStatementLogger.formatSql = false
        sessionFactory.statistics.statisticsEnabled = true
        enabled = true
        initialized = true

        sqlLogger = SqlLogger.create(System.out, logToConsole)
        System.out = sqlLogger
    }

    void disableMetrics() {
        System.out = sqlLogger.underlying

        sessionFactory.settings.sqlStatementLogger.logToStdout = false
        sessionFactory.statistics.statisticsEnabled = false
        enabled = false
    }


    void clearStats() {
        log.debug "clearing hibernate statistics"
        sessionFactory.statistics.clear()
        sqlLogger?.clear()
    }


    void markStartTime() {
        startMillis = System.currentTimeMillis()
    }

    void markActionEndTime() {
        actionTime = System.currentTimeMillis() - startMillis
    }

    void markViewEndTime() {
        totalTime = System.currentTimeMillis() - startMillis
        viewTime = totalTime - actionTime
    }


    Map getMetricsMap() {
        log.debug "reading hibernate statistics data"
        Map metrics = [ "Time Metrics":getTimeMetrics(),
                        "DB Metrics":getDatabaseMetrics() ]
        log.debug "hibernate statistics data read"
        metrics
    }


    private Map getTimeMetrics() {
        Map timeMetrics = [
            (TIME.toString()): [
                'Total': totalTime,
                'Controller/Service': actionTime,
                'View': viewTime
            ]
        ]
    }

    // note: must use recompiled Hibernate core jar file for criteria query stats to be included.
    private Map getDatabaseMetrics() {
        Statistics stats = sessionFactory.getStatistics()

        //println "loading metrics - collection roles = ${stats.collectionRoleNames}"
        //println "all props = ${stats.properties}\n\n"

        Map databaseMetrics

        // necessary to synchronize this? (as per http://stackoverflow.com/questions/8416366/hibernate-profiling)
        //synchronized (stats) {
        def loggedQueries = getLoggedQueries()
        def queryStats = getQueryStats( stats )
        def slowestQuery = getSlowestQuery( stats )
        def entityStats = getEntityStats( stats )
        def collectionStats = getCollectionStats( stats )
        def secondLevelCacheStats = getSecondLevelCacheStats( stats )

        databaseMetrics = [
            (COUNTS.toString()): [
                'Queries Executed': stats.queryExecutionCount,
                'Prepared Statements': stats.prepareStatementCount,
                'Transactions': stats.transactionCount,
                'Flushes': stats.flushCount
            ],
            (SQL.toString()): [
                'Logged to Console': loggedQueries,
                'Executed': stats.queries as List,
                'Stats': queryStats,
                'Slowest': slowestQuery
            ],
            (DOMAINS.toString()): [
                'Entities': entityStats,
                'Collections': collectionStats
            ],
            (QUERY_CACHE.toString()): [
                'Hit': stats.queryCacheHitCount,
                'Miss': stats.queryCacheMissCount,
                'Put': stats.queryCachePutCount
            ],
            (SECOND_LEVEL_CACHE.toString()): [
                'Hit': stats.secondLevelCacheHitCount,
                'Miss': stats.secondLevelCacheMissCount,
                'Put': stats.secondLevelCachePutCount,
                'Domains': secondLevelCacheStats
            ],
            (SESSIONS.toString()): [
                'Opened': stats.sessionOpenCount,
                'Closed': stats.sessionCloseCount
            ]
        ]
        //}
        databaseMetrics
    }



    // read second level cache stats out of the overall hibernate stats obj
    private Map getSecondLevelCacheStats( stats ) {
        def regionNames = stats.secondLevelCacheRegionNames
        Map map = [:]

        regionNames?.each { regionName ->
            def secondLevelStats = stats.getSecondLevelCacheStatistics( regionName )
            def statList = []

            def inMemory = secondLevelStats.elementCountInMemory
            def onDisk = secondLevelStats.elementCountOnDisk
            def hitCount = secondLevelStats.hitCount
            def missCount = secondLevelStats.missCount
            def putCount = secondLevelStats.putCount
            def sizeInMemory = secondLevelStats.sizeInMemory

            // this blows up when trying to read the Hibernate entries, so don't
            def entryIds
            if ( ! regionName.startsWith('org.hibernate') )
                entryIds = secondLevelStats.entries?.keySet()

            if ( inMemory ) statList << "Elements In Memory: $inMemory"
            if ( onDisk ) statList << "Elements On Disk: $onDisk"
            if ( hitCount ) statList << "Hits: $hitCount"
            if ( missCount ) statList << "Miss: $missCount"
            if ( putCount ) statList << "Put: $putCount"
            if ( sizeInMemory ) statList << "Memory Size: $sizeInMemory"
            if ( entryIds ) statList << "IDs: $entryIds"

            if ( statList )
                map.put( regionName, statList.flatten() )
        }

        map.sort()
    }

    // read collection stats out of the overall hibernate stats obj
    private Map getCollectionStats( stats ) {
        def collections = stats.collectionRoleNames
        Map map = [:]

        collections?.each { collection ->
            def collectionStats = stats.getCollectionStatistics( collection )
            def statList = []

            def deleteCount = collectionStats.removeCount
            def fetchCount = collectionStats.fetchCount
            def insertCount = collectionStats.recreateCount
            def loadCount = collectionStats.loadCount
            def updateCount = collectionStats.updateCount

            if ( deleteCount ) statList << "Removed: $deleteCount"
            if ( fetchCount ) statList << "Fetch: $fetchCount"
            if ( insertCount ) statList << "Recreated: $insertCount"
            if ( loadCount ) statList << "Load: $loadCount"
            if ( updateCount ) statList << "Update: $updateCount"

            if ( statList )
                map.put( collection, statList.flatten() )
        }

        map.sort()
    }


    // read entity stats out of the overall hibernate stats obj
    private Map getEntityStats( stats ) {
        def entities = stats.entityNames
        Map map = [:]

        entities?.each { entity ->
            def entityStats = stats.getEntityStatistics( entity )
            def statList = []

            def deleteCount = entityStats.deleteCount
            def fetchCount = entityStats.fetchCount
            def insertCount = entityStats.insertCount
            def loadCount = entityStats.loadCount
            def updateCount = entityStats.updateCount
            def optFailCount = entityStats.optimisticFailureCount

            if ( deleteCount ) statList << "Delete: $deleteCount"
            if ( fetchCount ) statList << "Fetch: $fetchCount"
            if ( insertCount ) statList << "Insert: $insertCount"
            if ( loadCount ) statList << "Load: $loadCount"
            if ( updateCount ) statList << "Update: $updateCount"
            if ( optFailCount ) statList << "State Object Exceptions: $optFailCount"

            if ( statList )
                map.put( entity, statList.flatten() )
        }

        map.sort()
    }


    // read query stats out of the overall hibernate stats obj
    private Map getQueryStats( stats ) {
        def queries = stats.queries
        Map map = [:]

        queries?.each { query ->
            def queryStats = stats.getQueryStatistics( query )
            def statList = [:]

            def executionCount = queryStats.executionCount
            def executionAvgTime = queryStats.executionAvgTime
            def executionMaxTime = queryStats.executionMaxTime
            def executionMinTime = queryStats.executionMinTime
            def executionRowCount = queryStats.executionRowCount

            if ( executionCount )
                statList.put( "Execution Count", executionCount )
            if ( executionAvgTime )
                statList.put( "Avg", "${executionAvgTime}ms" )
            if ( executionMaxTime )
                statList.put( "Max", "${executionMaxTime}ms" )
            if ( executionMinTime )
                statList.put( "Min", "${executionMinTime}ms" )
            if ( executionRowCount )
                statList.put( "Row Count", executionRowCount )
            if ( executionCount && executionAvgTime )
                statList.put( "(Avg*Count)", executionCount * executionAvgTime )

            if ( statList )
                map.put( query, statList )
        }

        // sort from most to least time, then convert values to string for display
        map.sort {
            def x = it.value['(Avg*Count)']
            x ? -x : it.value
        }.collectEntries { k, v ->
            [ (SqlFormatter.format(k)), v?.toString() ]
        }
    }

    private def getLoggedQueries() {
        sqlLogger?.readQueries()?.collectEntries { k, v ->
            [ (SqlFormatter.format(k)), ["Execution Count: $v"] ]
        }
    }

    private def getSlowestQuery( stats ) {
        def slowestQuery = stats.queryExecutionMaxTimeQueryString
        if ( slowestQuery ) {
            [ (SqlFormatter.format(slowestQuery)) :
                ["Execution Time: ${stats.queryExecutionMaxTime}ms"] ]
        }
        else ''
    }

}
