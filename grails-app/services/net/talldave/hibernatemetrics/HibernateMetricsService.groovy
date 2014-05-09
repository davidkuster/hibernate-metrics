package net.talldave.hibernatemetrics

import grails.util.Holders
import org.hibernate.stat.Statistics
import net.talldave.hibernatemetrics.util.SqlLogger
import net.talldave.hibernatemetrics.util.SqlFormatter


class HibernateMetricsService {

    static transactional = false

    def sessionFactory

    def config = Holders.config

    boolean enabled = config.grails.plugins.hibernateMetrics.enabled ?: false
    boolean logToConsole = config.grails.plugins.hibernateMetrics.logSqlToConsole ?: true
    def excludeActions = [ 'hibernateMetrics':['*'] ] << config.grails.plugins.hibernateMetrics.excludeActions

    def initialized

    def startMillis
    def actionTime
    def totalTime
    def viewTime

    def sqlLogger



    def shouldTrack( controllerName, actionName ) {
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


    def enableMetrics() {
        sessionFactory.settings.sqlStatementLogger.logToStdout = true
        sessionFactory.settings.sqlStatementLogger.formatSql = false
        sessionFactory.statistics.statisticsEnabled = true
        enabled = true
        initialized = true

        sqlLogger = SqlLogger.create(System.out, logToConsole)
        System.out = sqlLogger
    }

    def disableMetrics() {
        System.out = sqlLogger.underlying

        sessionFactory.settings.sqlStatementLogger.logToStdout = false
        sessionFactory.statistics.statisticsEnabled = false
        enabled = false
    }



    def markStartTime() {
        startMillis = System.currentTimeMillis()
    }

    def markActionEndTime() {
        actionTime = System.currentTimeMillis() - startMillis
    }

    def markViewEndTime() {
        totalTime = System.currentTimeMillis() - startMillis
        viewTime = totalTime - actionTime
    }


    def getMetricsMap() {
        log.debug "reading hibernate statistics data"
        def metrics = [ "Time Metrics":getTimeMetrics(),
            "DB Metrics":getDatabaseMetrics() ]
        log.debug "hibernate statistics data read"
        metrics
    }


    private def getTimeMetrics() {
        def timeMetrics = [
            'Total Time (ms)': totalTime,
            'Controller/Service (ms)': actionTime,
            'View (ms)': viewTime
        ]
        timeMetrics
    }

    private def getDatabaseMetrics() {
        Statistics stats = sessionFactory.getStatistics()

        // note: must use recompiled Hibernate core jar file
        // for criteria query stats to be included.

        def collectionStats = getCollectionStats( stats )
        def entityStats = getEntityStats( stats )
        def queryStats = getQueryStats( stats )
        def loggedQueries = getLoggedQueries()
        def slowestQuery = getSlowestQuery( stats )
        def secondLevelCacheStats = getSecondLevelCacheStats( stats )

        //println "loading metrics - collection roles = ${stats.collectionRoleNames}"
        //println "all props = ${stats.properties}\n\n"

        def databaseMetrics = [
            'Total Queries': stats.queryExecutionCount,
            'Prepared Statements': stats.prepareStatementCount,
            'Logged SQL': loggedQueries,
            //'Queries': stats.queries as List,
            'Query Stats': queryStats,
            'Slowest Query': slowestQuery,
            'Collection Info': collectionStats,
            'Entity Info': entityStats,
            'Query Cache Hit': stats.queryCacheHitCount,
            'Query Cache Miss': stats.queryCacheMissCount,
            'Query Cache Put': stats.queryCachePutCount,
            'Second Level Cache': secondLevelCacheStats,
            'Sessions Opened': stats.sessionOpenCount,
            'Sessions Closed': stats.sessionCloseCount,
            'Transaction Count': stats.transactionCount,
            'Flush Count': stats.flushCount
        ]

        databaseMetrics
    }


    def clearStats() {
        log.debug "clearing hibernate statistics"
        sessionFactory.statistics.clear()
        sqlLogger?.clear()
    }


    // read second level cache stats out of the overall hibernate stats obj
    private def getSecondLevelCacheStats( stats ) {
        def regionNames = stats.secondLevelCacheRegionNames
        def map = [:]

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
            def entries
            if ( ! regionName.startsWith('org.hibernate') )
                entries = secondLevelStats.entries

            if ( inMemory ) statList << "Elements In Memory: $inMemory"
            if ( onDisk ) statList << "Elements On Disk: $onDisk"
            if ( hitCount ) statList << "Hits: $hitCount"
            if ( missCount ) statList << "Miss: $missCount"
            if ( putCount ) statList << "Put: $putCount"
            if ( sizeInMemory ) statList << "Memory Size: $sizeInMemory"
            //if ( entries ) statList << "Entries: $entries"

            if ( statList )
                map.put( regionName, statList.flatten() )
        }

        map.sort()
    }

    // read collection stats out of the overall hibernate stats obj
    private def getCollectionStats( stats ) {
        def collections = stats.collectionRoleNames
        def map = [:]

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
    private def getEntityStats( stats ) {
        def entities = stats.entityNames
        def map = [:]

        entities?.each { entity ->
            def entityStats = stats.getEntityStatistics( entity )
            def statList = []

            def deleteCount = entityStats.deleteCount
            def fetchCount = entityStats.fetchCount
            def insertCount = entityStats.insertCount
            def loadCount = entityStats.loadCount
            def updateCount = entityStats.updateCount

            if ( deleteCount ) statList << "Delete: $deleteCount"
            if ( fetchCount ) statList << "Fetch: $fetchCount"
            if ( insertCount ) statList << "Insert: $insertCount"
            if ( loadCount ) statList << "Load: $loadCount"
            if ( updateCount ) statList << "Update: $updateCount"
            // not including optimisticFailureCount for now

            if ( statList )
                map.put( entity, statList.flatten() )
        }

        map.sort()
    }


    // read query stats out of the overall hibernate stats obj
    private def getQueryStats( stats ) {
        def queries = stats.queries
        def map = [:]

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
            [ (SqlFormatter.format(k)), v as String ]
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
