hibernate-metrics
=================

Grails plugin to report some simple application performance metrics using the Hibernate Statistics API.


Upon installing the plugin, the following will need to be added to Config.groovy:

    // Hibernate Metrics plugin config
    grails.plugins.hibernateMetrics.excludeActions = [
      // either controllerName or actionName can be replaced with '*'
      // 'controllerName':['actionName', 'action2', 'etc']
      'hibernateMetrics':['*']
    ]

    // enabled at startup
    grails.plugins.hibernateMetrics.enabled = false
    // pretty-format SQL queries
    grails.plugins.hibernateMetrics.formatSQL = false
    // write SQL to console like logSql=true normally does
    grails.plugins.hibernateMetrics.logSqlToConsole = true

The 'excludeActions' setting will bypass metrics tracking for the specified controller actions.

'enabled' = whether it should happen at startup

'formatSQL' = whether to pretty print the SQL

'logSqlToConsole' = whether the SQL will be logged to the console as the normal DataSource logSql=true setting operates.  Note that this should probably be set to false for production environments (?)...


Put this somewhere in the UI, typically in a footer or header...

    <hibernateMetrics:devEnvControl/>

This will show Enable/Disable link only in the development environment.  In other environments the metrics can be enabled or disabled by navigating to the following URLs:

    /appName/hibernateMetrics/enableMetrics
    /appName/hibernateMetrics/disableMetrics


Then wherever you want to show the metrics - once they are enabled, use this tag:

    <hibernateMetrics:metrics/>

This can also be used with these additional tags, to control where the metrics are displayed.  For instance, if there is a static header at the top of the page it can be replaced when the metrics are turned on:

    <hibernateMetrics:isNotEnabled>
        <img src="/myStaticBanner.png"/>
    </hibernateMetrics:isNotEnabled>

    <hibernateMetrics:isEnabled>
        <hibernateMetrics:metrics/>
    </hibernateMetrics:isEnabled>