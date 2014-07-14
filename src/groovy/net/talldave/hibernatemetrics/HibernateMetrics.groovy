package net.talldave.hibernatemetrics

import static net.talldave.hibernatemetrics.DatabaseMetricsType.*

import grails.util.Holders


class HibernateMetrics {

    static HibernateMetricsService metricsService


    static void initService() {
        if ( ! metricsService ) {
            metricsService = Holders.grailsApplication.mainContext.getBean('hibernateMetricsService')
        }
    }


    // apparently not possible to use varargs and inline closure param - bummer
    //static void withSqlLogging( DatabaseMetricsType... types = [ALL], Closure c ) {
    static void withSqlLogging( List<DatabaseMetricsType> types = [ALL], Closure c ) {
        if ( ! c ) return

        initService()

        metricsService.enableMetrics()
        metricsService.clearStats()
        metricsService.markStartTime()

        def codeResults = c.call()

        metricsService.markActionEndTime()
        metricsService.markViewEndTime()
        metricsService.disableMetrics()

        def metricsResults = metricsService.getMetricsMap()

        displayMetricsResults(metricsResults, types)
        displayCodeResults(codeResults)
    }


    static private void displayMetricsResults(Map results, List<DatabaseMetricsType> types) {
        println """
---------- Hibernate Metrics ----------

Time Metrics:
${formatMetrics( results['Time Metrics'] )}

DB Metrics:
${formatMetrics( results['DB Metrics'], types )}

---------------------------------------
"""
    }

    static private String formatMetrics( Map metrics, List types = null ) {
        def typeNames = types*.toString()

        metrics.collect { k, v ->
            if ( typeNames && ! (typeNames =~ ALL.toString()) && ! (typeNames =~ k) ) {
                // skip excluded metrics types
                return
            }
            else if ( v in Map ) {
                formatMetricsMap( k, v )
            }
            else {
                "  $k = $v"
            }
        }.findAll { it }.join('\n')
    }


    static private String formatMetricsMap( String key, Map val, indent=2 ) {
        def map = val.collect { a, b ->
            if ( b in Map ) {
                formatMetricsMap( a, b, indent + 2 )
            }
            else {
                "\n${' ' * (indent + 2)}$a = $b"
            }
        }.join('')
        "\n${' ' * indent}$key = $map"
    }


    static private void displayCodeResults(codeResults) {
        println codeResults
    }

}