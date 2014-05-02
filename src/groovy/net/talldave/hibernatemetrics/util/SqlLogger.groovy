package net.talldave.hibernatemetrics.util

// found at http://stackoverflow.com/questions/4334808/how-could-i-read-java-console-output-into-a-string-buffer

class SqlLogger extends PrintStream {

    final StringBuilder buf
    final PrintStream underlying

    def sql = [:]


    public SqlLogger(StringBuilder sb, OutputStream os, PrintStream ul) {
        super(os)
        buf = sb
        underlying = ul
    }


    public static SqlLogger create(PrintStream toLog, final boolean logToConsole) {
        try {
            final StringBuilder sb = new StringBuilder()
            OutputStream psout = toLog.out

            return new SqlLogger(
                sb,
                new FilterOutputStream(psout) {
                    public void write(int b) {
                        super.write(b)
                        //if ( logToConsole )
                          sb.append((char) b)
                    }
                },
                toLog)
        }
        catch ( e ) {}
    }

    def readQueries() {
        def s = buf?.toString()
        // clear buffer so that data isn't potenially parsed more than once
        buf?.setLength(0)

        s?.eachLine { line ->
            if ( line?.startsWith('Hibernate:') || line?.startsWith('[CRITERIA]') ) {
                def executionCount = sql.get( line, 0 ) // default to 0
                sql.put( line, ++executionCount )
            }
        }

        // sort sql map from highest execution count to lowest
        //return sql.sort { a, b -> a.value <=> b.value }
        return [:] + sql.entrySet().sort{it.value}.reverse()
    }

    def clear() {
        buf?.setLength(0)
        sql.clear()
    }

}