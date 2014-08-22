package net.talldave.hibernatemetrics.util

import org.springframework.util.ClassUtils

class HibernateVersionUtil {

    private static final boolean hibernate3Present = ClassUtils.isPresent("org.hibernate.connection.ConnectionProvider", HibernateVersionUtil.class.getClassLoader())

    static boolean isHibernate3() {
        return hibernate3Present
    }

    static boolean isHibernate4() {
        return ! hibernate3Present
    }

}