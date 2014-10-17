Grails Hibernate Metrics Plugin
===============================

Grails plugin to report some simple application performance metrics using the Hibernate Statistics API.  The plugin was introduced at [GR8Conf US 2014](http://gr8conf.us) - the presentation video is available on [YouTube](https://www.youtube.com/watch?v=_cdb7zYNEFg).

Note that this plugin is in a very early stage and should be considered experimental.  At this point I suggest only including it in a development or test environment.  This can be done by including the plugin in BuildConfig.groovy as follows. (Where x.y.z is obviously replaced with the desired version of the plugin.)

    import grails.util.Environment

    // in the plugins block
    if (Environment.current != Environment.PRODUCTION) {
        compile ":hibernate-metrics:x.y.z"
    }
**Be advised!  If you exclude the plugin from the production environment you will also need to wrap any taglib calls (such as &lt;hibernateMetrics:metrics&gt;) in a similar block or those will blow up.  In production.  Which would be bad.**

*Note as well that the plugin supports both Hibernate 3 and 4, but does not include a specific dependency for either.  It is also assumed that some version of jQuery and jQuery UI is available.*


##Setup

Upon installing the plugin, the following additional config options can be added to Config.groovy.

    grails.plugins.hibernateMetrics.excludeActions = [
      // either controllerName or actionName can be replaced with '*'
      'controllerName':['actionName', 'action2', 'etc'],
      'controllerName2':['actionName2', 'etc']
    ]

    // enabled at startup
    grails.plugins.hibernateMetrics.enabled = false
    // write SQL to console like logSql=true normally does
    grails.plugins.hibernateMetrics.logSqlToConsole = true

'excludeActions' - bypass metrics tracking for specified controller actions.

'enabled' - if the plugin should be enabled at application startup.

'logSqlToConsole' - if the SQL should be logged to the console as the normal DataSource logSql=true setting operates.  *(Note that currently this setting is not working as desired if set to false. PRs welcome.) :)*


##Application Integration

The metrics tracking and display can be turned on and off.  A convenience option is provided to let the developer decide when they are interested in the working with the plugin.  To enable this option put the following tag in your UI, typically in a footer or header:

    <hibernateMetrics:devEnvControl/>

In the development environment this will display an "Enable" or "Disable" link which will allow you to turn the metrics tracking and display on or off with a single click.  In other environments the metrics can be manually enabled or disabled by navigating to the following URLs:

    /appName/hibernateMetrics/enableMetrics
    /appName/hibernateMetrics/disableMetrics


It's also necessary to include the metrics display in your application's user interface.  Wherever you want to show the metrics - once they are enabled - use this tag:

    <hibernateMetrics:metrics/>

This can also be used with these additional tags, to control where the metrics are displayed.  For instance, if there is a static header at the top of the page it can be replaced when the metrics are turned on:

    <hibernateMetrics:isNotEnabled>
        <img src="/myStaticBanner.png"/>
    </hibernateMetrics:isNotEnabled>

    <hibernateMetrics:isEnabled>
        <hibernateMetrics:metrics/>
    </hibernateMetrics:isEnabled>


##Metrics Reported

__Time Metrics__

- Total = overall time to complete the request
- Controller/Service = time spent in the filter/command object/controller/service/domain layers
- View = time spent in the GSP/taglib layers

__Database Metrics__

(Refer to the [Hibernate Statistics API](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html) for more info.)

- Counts

  - [Queries Executed](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getQueryExecutionCount()) = global number of executed queries
  - [Prepared Statements](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getPrepareStatementCount()) = number of prepared statements that were acquired
  - [Transactions](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getTransactionCount()) = number of transactions we know to have completed
  - [Flushes](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getFlushCount()) = global number of flushes executed by sessions (either implicit or explicit)

- SQL

  - Logged to Console = the queries logged to the console (ala logSql=true)
  - Query Stats = statistics on individual queries as reported by Hibernate, including number of times executed, max/min/average time taken and number of rows returned
  - [Slowest Query](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getQueryExecutionMaxTimeQueryString()) = query string for the slowest query

- Domains

  - Entities = details of domain objects retrieved
  - Collections = details of collections retrieved

- Query Cache

  - [Hit](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getQueryCacheHitCount()) = global number of cached queries successfully retrieved from cache
  - [Miss](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getQueryCacheMissCount()) = global number of cached queries *not* found in cache
  - [Put](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getQueryCachePutCount()) = global number of cacheable queries put in cache

- 2L Cache

  - [Hit](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getSecondLevelCacheHitCount()) = global number of cacheable entities/collections successfully retrieved from cache
  - [Miss](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getSecondLevelCacheMissCount()) = global number of cacheable entities/collections not found in the cache and loaded from the database
  - [Put](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getSecondLevelCachePutCount()) = global number of cacheable entities/collections put in the cache
  - Domains = details of domain objects in the cache

- Sessions

  - [Opened](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getSessionOpenCount()) = global number of sessions opened
  - [Closed](http://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/stat/Statistics.html#getSessionCloseCount()) = global number of sessions closed

##Programmatic Execution

In addition to a heads-up display, the metrics tracking can be selectively applied to specific blocks of code.  This has primarily been used in the [Grails Console Plugin](http://grails.org/plugin/console) but could theoretically be integrated directly into application code.  *(Note that currently the plugin is not configured to allow for this approach to be turned on or off, so it is certainly not recommended for use in production.)*

To programmatically examine blocks of code, use the following:

    import static net.talldave.hibernatemetrics.HibernateMetrics

    HibernateMetrics.withSqlLogging {
        // code to be examined
    }

Note as well that this can also be called as follows:

    import static net.talldave.hibernatemetrics.HibernateMetrics
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

Note that ALL is the default if no types are specified.