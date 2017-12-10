package info.akang.groovy.transform

import groovy.sql.Sql

class SqlAssistMethodUsage {

    Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")

    @SqlAssist
    def run() {
        DBUtil.dropAllTables(sql)

        sql.execute """
            CREATE TABLE test1
            (
                id INTEGER,
                some_date DATE,
                qty SMALLINT,
                price DECIMAL(6,2)
            );"""

        insert test1 with sql
        id     | some_date      | qty   | price
        1      | '2017-01-05'   | 2     | 1.99
    }
}
