log4j = {
    error 'org.codehaus.groovy.grails',
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'
}

grails.plugins.hibernateMetrics.excludeActions = [
    // either controllerName or actionNames can be replaced with '*'
    // 'controllerName':['actionName', 'action2', 'etc'],
    // 'otherController':['otherAction', 'otherAction2', 'etc']
]

// turned on by default
grails.plugins.hibernateMetrics.enabled = false
// write SQL to console like logSql=true normally does
grails.plugins.hibernateMetrics.logSqlToConsole = true
