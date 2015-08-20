import net.talldave.hibernatemetrics.HibernateMetrics

class HibernateMetricsGrailsPlugin {

    def version = '0.1.1'
    def grailsVersion = '2.0 > *'
    def title = 'Hibernate Metrics'
    def author = 'David Kuster'
    def authorEmail = 'dave@talldave.net'
    def description = '''\
Reports simple application performance metrics using the Hibernate Statistics API.  Provides timing information, queries run, collections and entities loaded, as well as transaction and flush counts.
'''
    def documentation = 'http://grails.org/plugin/hibernate-metrics'
    def license = 'APACHE'
    def issueManagement = [system: 'GITHUB', url: 'http://github.com/davidkuster/hibernate-metrics/issues']
    def scm = [ url: 'http://github.com/davidkuster/hibernate-metrics' ]

    def doWithApplicationContext = { ctx ->
        HibernateMetrics.metricsService = ctx.hibernateMetricsService
    }
}
