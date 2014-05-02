package net.talldave.hibernatemetrics

import grails.converters.JSON


class HibernateMetricsController {

    def hibernateMetricsService

    // get the metrics display via ajax so it can include information about the view rendering
    def ajaxDisplayMetrics(Integer loadNum) {
        // read metrics just before being displayed
        session.metrics = hibernateMetricsService.getMetricsMap()
        // on first load, clear stats after metrics are read - after that it's manual
        if ( loadNum == 0 ) {
            hibernateMetricsService.clearStats()
        }
        render( template:"/hibernateMetrics/displayMetrics",
                plugin:"hibernate-metrics" )
    }

    def ajaxClearStats() {
        hibernateMetricsService.clearStats()
        session.metrics = null
        render( ["success":"true"] as JSON )
    }

    // action to enable metrics on the fly
    def enableMetrics() {
        hibernateMetricsService.enableMetrics()
        flash.message = "Performance Metrics Enabled"
        doRedirect(request)
    }

    // action to disable metrics on the fly
    def disableMetrics() {
        hibernateMetricsService.disableMetrics()
        flash.message = "Performance Metrics Disabled"
        doRedirect(request)
    }


    // redirect user back to previous URL,
    // unless previous URL is this URL.
    // in that case send to root to avoid loop
    private def doRedirect( request ) {
        def referer = request.getHeader("Referer")
        if ( referer?.indexOf( "hibernateMetrics" ) == -1 )
            redirect( url:referer )
        else
            redirect( uri:"/" )
    }

}
