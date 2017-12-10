package info.akang.groovy.transform

import org.codehaus.groovy.ast.stmt.Statement

class SqlAssistReplacement {

    boolean colHeaderLineProcessed = false

    /**
     * Name of Sql variable name
     */
    String sql

    String tablename

    List<String> columnHeaders = []

    List<List<String>> dataLines = []

    /**
     * Index start of statements that need to be replaced with dataLines - inclusive
     */
    int stmtIndexStart = 0

    /**
     * Index start of statements that need to be replaced with dataLines - exclusive
     */
    int stmtIndexEnd = 0

    List<Statement> replacementStatements

    void addDataLine(List<String> dataLine) {
        dataLines << dataLine
    }

    void addColumnHeaderLine(List<String> dataLine) {
        columnHeaders = dataLine
        colHeaderLineProcessed = true
    }
}
