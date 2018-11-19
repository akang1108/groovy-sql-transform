package info.akang.groovy.transform

import groovy.sql.Sql

class SqlAssistChars {

    Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")

    @SqlAssist
    def run() {
        DBUtil.dropAllTables(sql)

        sql.execute """
            CREATE TABLE test_table
            (
                id INTEGER,
                some_text VARCHAR
            );"""

        insert test_table with sql
        id     | some_text
        1      | "abcde"
    }
}
