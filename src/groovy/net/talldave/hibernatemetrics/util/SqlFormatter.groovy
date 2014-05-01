package net.talldave.hibernatemetrics.util

import org.hibernate.jdbc.util.BasicFormatterImpl // Hibernate 3.3.2 to pre-4.0
// use org.hibernate.engine.jdbc.internal.BasicFormatterImpl as of Hibernate 4.0


class SqlFormatter {

  private static formatter

  static getFormatter() {
    if ( ! formatter ) {
      // TODO: determine how to make this work depending on which version of Hibernate is currently in use
      formatter = new BasicFormatterImpl()
    }
    formatter
  }

  static String format(String query) {
    if ( query )
      getFormatter().format(query)
  }

  static String formatForHtml(String query) {
    format(query)?.replaceAll('\n','<br>')
  }

}