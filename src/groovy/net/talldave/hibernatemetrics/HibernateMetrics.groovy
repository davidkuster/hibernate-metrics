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

    c.call()

    metricsService.markActionEndTime()
    metricsService.markViewEndTime()
    metricsService.disableMetrics()

    def results = metricsService.getMetricsMap()

    displayResults(results)
  }


  static private void displayResults(Map results) {
    println "\nTime Metrics: "
    results['Time Metrics'].each { println it }
    println "\nDB Metrics: "
    results['DB Metrics'].each { println it }
  }

}