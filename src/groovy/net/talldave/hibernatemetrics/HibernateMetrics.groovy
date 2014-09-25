package net.talldave.hibernatemetrics

import static net.talldave.hibernatemetrics.MetricsType.*


class HibernateMetrics {

    // initialized in doWithApplicationContext
    static HibernateMetricsService metricsService


    // withSqlLogging can be called in multiple ways:
    // HibernateMetrics.withSqlLogging { /* code to have sql logged */ }
    // HibernateMetrics.withSqlLogging(TIME, SQL) { /* code to have sql logged */ }
    // HibernateMetrics.withSqlLogging([TIME, SQL]) { /* code to have sql logged */ }
    // last two examples indicate which metrics to report on, using MetricsType enum

    static void withSqlLogging(... params) {
        List types = params as List
        Closure c = types?.pop()

        if ( ! c || ! types?.every { it in MetricsType } ) {
            throw new IllegalArgumentException( "invalid parameters to withSqlLogging $params - expects MetricsType enum values (optional) and closure" )
        }

        withSqlLogging( types, c )
    }

    static void withSqlLogging(List<MetricsType> types, Closure c) {

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


    static private void displayMetricsResults(Map results, List<MetricsType> types) {
        def timeMetrics = formatMetrics( results['Time Metrics'], types )
        def dbMetrics = formatMetrics( results['DB Metrics'], types )

        StringBuilder sb = new StringBuilder()
        if ( timeMetrics ) sb.append( "\nTime Metrics:\n$timeMetrics\n" )
        if ( dbMetrics ) sb.append( "\nDB Metrics:\n$dbMetrics\n" )

        println """
---------- Hibernate Metrics ----------
${sb}
---------------------------------------
"""
    }

    static private String formatMetrics( Map metrics, List types ) {
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
