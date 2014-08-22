package net.talldave.hibernatemetrics.util

class SqlFormatter {

  private static formatter

  static getFormatter() {
    if ( ! formatter ) {
      if ( HibernateVersionUtil.isHibernate3() ) {
        formatter = Class.forName('org.hibernate.jdbc.util.BasicFormatterImpl').newInstance()
      }
      else if ( HibernateVersionUtil.isHibernate4() ) {
        formatter = Class.forName('org.hibernate.engine.jdbc.internal.BasicFormatterImpl').newInstance()
      }
      else {
        throw new UnsupportedOperationException("Unknown Hibernate version")
      }
    }
    formatter
  }

  static String format(String query) {
    if ( query )
      getFormatter().format(query).trim()
  }

  static String formatForHtml(String query) {
    format(query)?.replaceAll('\n','<br>')
  }

}