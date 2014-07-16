hibernate-metrics
=================

Grails plugin to report some simple application performance metrics using the Hibernate Statistics API.


##Setup

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
- removed - changed to always pretty-print the SQL...

'logSqlToConsole' = whether the SQL will be logged to the console as the normal DataSource logSql=true setting operates.  Note that this should probably be set to false for production environments (?)...


##Application Integration

It's necessary to include the metrics display in your application's user interface...

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


##Metrics Reported

 - Collections/Entities
   - fetch = loaded from DB
   - load = loaded from DB or cache

TBD
 - more details


##Programmatic Execution

The metrics can be selectively applied to specific blocks of code.  This has primarily been used in the [Grails Console Plugin](http://grails.org/plugin/console) but can be integrated directly into application code.

Note that currently the plugin is not configured to allow for this approach to be turned on or off, so it is not recommended for use in production.

To programmatically examine blocks of code, use the following:

    HibernateMetrics.withSqlLogging {
        // code to be examined
    }

Note as well that this can also be called as follows:

    import static net.talldave.hibernatemetrics.MetricsType.*

    HibernateMetrics.withSqlLogging(TIME, SQL) {
        // code to be examined
    }

    def metricsTypes = [TIME, COUNTS, SQL, DOMAINS]
    HibernateMetrics.withSqlLogging(types) {
        // code to be examined
    }

The parameters in the last two examples indicate which metrics to report on, by using the MetricsType enum.  The possible enum values are:

    ALL
    TIME
    COUNTS
    SQL
    DOMAINS
    QUERY_CACHE
    SECOND_LEVEL_CACHE
    SESSIONS