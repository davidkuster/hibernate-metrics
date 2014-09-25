package net.talldave.hibernatemetrics

// inspired by / taken from :
// http://www.intelligrape.com/blog/2011/11/07/grails-find-number-of-queries-executed-for-a-particular-request/
class HibernateMetricsFilters {

    def hibernateMetricsService


    def filters = {

        logHibernateStats(controller: '*', action: '*') {
            before = {
                if ( hibernateMetricsService?.shouldTrack( controllerName, actionName ) ) {
                    hibernateMetricsService.markStartTime()
                }
            }

            after = {
                if ( hibernateMetricsService?.shouldTrack( controllerName, actionName ) ) {
                    hibernateMetricsService.markActionEndTime()
                }
            }

            afterView = { e ->
                if ( hibernateMetricsService?.shouldTrack( controllerName, actionName ) ) {
                    hibernateMetricsService.markViewEndTime()
                }
            }

        }

    } // end filters block

}
