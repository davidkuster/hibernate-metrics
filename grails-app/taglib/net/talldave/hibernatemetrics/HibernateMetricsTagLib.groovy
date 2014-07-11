package net.talldave.hibernatemetrics

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import grails.util.Environment


class HibernateMetricsTagLib {

    static namespace = "hibernateMetrics"

    def hibernateMetricsService



    def isEnabled = { attrs, body ->
        if ( metricsEnabled() )
            out << body()
    }

    def isNotEnabled = { attrs, body ->
        if ( ! metricsEnabled() )
            out << body()
    }


    private def metricsEnabled() {
        def controllerName = request.getAttribute(GrailsApplicationAttributes.CONTROLLER_NAME_ATTRIBUTE)
        def actionName = request.getAttribute(GrailsApplicationAttributes.ACTION_NAME_ATTRIBUTE)
        hibernateMetricsService.shouldTrack( controllerName, actionName )
    }


    // tag renders template that will load the metrics display via AJAX
    // after the page has rendered so view timing can be included
    def metrics = {
        out << render( template:'/hibernateMetrics/loadMetrics',
                        plugin:"hibernate-metrics" )
    }


    // used to display individual metrics.
    // for time display, use this in the alt/title/mouseover/whatever:
    // ${endTime}ms (${(endTime/1000 as Double).round(2)}s) (${(endTime/1000/60 as Double).round(2)}m)
    def metricDisplay = { attrs ->
        def name = attrs.name
        def value = attrs.value

        def title = ""

        // hacking in time display
        if ( value && name =~ /\(ms\)/ ) {
            title = "${(value/1000 as Double).round(2)}s / ${(value/1000/60 as Double).round(2)}m"
        }

        // TODO: would be better to let service dictate severity level, per metric

        if ( value?.toString()?.isNumber() && value >= 100 )
            out << "<label title='$title' class='metricsWarning'>$value</label>"
        else if ( value?.toString()?.isNumber() && value >= 50 )
            out << "<label title='$title' class='metricsAttention'>$value</label>"
        else if ( value instanceof Map || value instanceof List )
            out << render( template:'/hibernateMetrics/metricCollectionView',
                            model:[value:value, name:name],
                            plugin:"hibernate-metrics" )
        else if ( ! value && value != 0 )
            out << 'N/A'
        else
            out << "<label title='$title' class='metricsValue'>${value}</label>"
    }


    def devEnvControl = { attrs ->
        if ( Environment.current == Environment.DEVELOPMENT ) {
            if ( ! metricsEnabled() )
                out << """Hibernate Metrics
                    [${g.link(controller:"hibernateMetrics", action:"enableMetrics") { "Enable" } }]"""
            else
                out << """Hibernate Metrics
                    [${g.link(controller:"hibernateMetrics", action:"disableMetrics") { "Disable" } }] """
        }
    }

}
