package net.talldave.hibernatemetrics

import grails.util.Holders
import org.hibernate.stat.Statistics
import net.talldave.hibernatemetrics.util.SqlLogger


class HibernateMetricsService {

    static transactional = false

    def sessionFactory

    def config = Holders.config

    boolean enabled = config.grails.plugins.hibernateMetrics.enabled
    def excludeActions = config.grails.plugins.hibernateMetrics.excludeActions
    def formatSQL = config.grails.plugins.hibernateMetrics.formatSQL

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

        sqlLogger = SqlLogger.create(System.out, formatSQL)
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


    def getTimeMetrics() {
        def timeMetrics = [
            'Total Time (ms)': totalTime,
            'Controller/Service (ms)': actionTime,
            'View (ms)': viewTime
        ]
        timeMetrics
    }



    def getDatabaseMetrics() {
        Statistics stats = sessionFactory.getStatistics()

        // note: must use recompiled Hibernate core jar file
        // for criteria query stats to be included.

        def entityStats = getEntityStats( stats )
        def queryStats = getQueryStats( stats )

        def loggedQueries = sqlLogger?.readQueries()

        def databaseMetrics = [
            'Total Queries': stats.queryExecutionCount,
            'Prepared Statements': stats.prepareStatementCount,
            'Logged SQL': loggedQueries,
            'Slowest Query Time (ms)': stats.queryExecutionMaxTime,
            'Slowest Query': [stats.queryExecutionMaxTimeQueryString] - null,
            'Queries': stats.queries as List,
            'Query Info': queryStats,
            'Collections Fetched (DB)': stats.collectionFetchCount,
            'Collections Loaded (DB or cache)': stats.collectionLoadCount,
            'Entities Fetched (DB)': stats.entityFetchCount,
            'Entities Loaded (DB or cache)': stats.entityLoadCount,
            'Entity Info': entityStats,
            'Transaction Count': stats.transactionCount,
            'Flush Count': stats.flushCount
        ]

        databaseMetrics
    }


    def clearStats() {
        sessionFactory.statistics.clear()
        sqlLogger?.clear()
    }


    // read entity stats out of the overall hibernate stats obj
    def getEntityStats( stats ) {
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

            if ( statList.size() > 0 )
                map.put( entity, statList.flatten() )
        }

        map.sort()
    }


    // read query stats out of the overall hibernate stats obj
    def getQueryStats( stats ) {
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

            if ( statList.size() > 0 )
                map.put( query, statList )
        }

        // sort from most to least time, then convert values to string for display
        map.sort {
            def x = it.value['(Avg*Count)']
            x ? -x : it.value
        }.collectEntries { k, v ->
            [ k, v as String ]
        }
    }

}
