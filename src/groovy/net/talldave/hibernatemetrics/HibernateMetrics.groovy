package net.talldave.hibernatemetrics

import grails.util.Holders

class HibernateMetrics {

  static HibernateMetricsService metricsService


  static void initService() {
    if ( ! metricsService ) {
      metricsService = Holders.grailsApplication.mainContext.getBean('hibernateMetricsService')
    }
  }


  static void withSqlLogging( Closure c ) {
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

    displayMetricsResults(metricsResults)
    displayCodeResults(codeResults)
  }


  static private void displayMetricsResults(Map results) {
    println """
-------------------------------------

Time Metrics:
${results['Time Metrics'].collect { k, v -> "  $k = $v" }.join('\n') }

DB Metrics:
${results['DB Metrics'].collect { k, v ->
  if ( v in Map ) {
    def map = v.collect { a, b ->
      "\n    $a = $b"
    }.join('')
    "\n  $k = $map\n"
  }
  else {
    "  $k = $v"
  }
}.join('\n') }

-------------------------------------
"""
  }


  static private void displayCodeResults(codeResults) {
    println codeResults
  }

}