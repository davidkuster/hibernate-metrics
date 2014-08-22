package net.talldave.hibernatemetrics.util

import org.hibernate.SessionFactory

class SessionFactoryHelper {

    static boolean logOutSetting = false
    static boolean formatSetting = false
    static boolean statsSetting = false


    static void enableStats( SessionFactory sessionFactory ) {
        if ( HibernateVersionUtil.isHibernate3() ) {
            logOutSetting = sessionFactory.settings.sqlStatementLogger.logToStdout
            formatSetting = sessionFactory.settings.sqlStatementLogger.formatSql

            sessionFactory.settings.sqlStatementLogger.logToStdout = true
            sessionFactory.settings.sqlStatementLogger.formatSql = false
        }
        else if ( HibernateVersionUtil.isHibernate4() ) {
            logOutSetting = sessionFactory.jdbcServices.sqlStatementLogger.logToStdout
            formatSetting = sessionFactory.jdbcServices.sqlStatementLogger.format

            sessionFactory.jdbcServices.sqlStatementLogger.logToStdout = true
            sessionFactory.jdbcServices.sqlStatementLogger.format = false
        }
        else {
            throw new UnsupportedOperationException("Unknown Hibernate version")
        }

        statsSetting = sessionFactory.statistics.statisticsEnabled
        sessionFactory.statistics.statisticsEnabled = true
    }


    // restore various config options to their previous settings
    // (instead of explicitly disabling everything)
    static void disableStats( SessionFactory sessionFactory ) {
        if ( HibernateVersionUtil.isHibernate3() ) {
            sessionFactory.settings.sqlStatementLogger.logToStdout = logOutSetting
            sessionFactory.settings.sqlStatementLogger.formatSql = formatSetting
        }
        else if ( HibernateVersionUtil.isHibernate4() ) {
            sessionFactory.jdbcServices.sqlStatementLogger.logToStdout = logOutSetting
            sessionFactory.jdbcServices.sqlStatementLogger.format = formatSetting
        }
        else {
            throw new UnsupportedOperationException("Unknown Hibernate version")
        }

        sessionFactory.statistics.statisticsEnabled = statsSetting
    }

}