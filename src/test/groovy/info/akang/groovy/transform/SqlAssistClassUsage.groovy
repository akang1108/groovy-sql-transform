package info.akang.groovy.transform

import groovy.sql.Sql

@SqlAssist
class SqlAssistClassUsage {

    Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")

    static {

    }

    def nothing() {}

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
        2      | '2017-01-05'   | 5     | 3.99

        insert test1 with sql
        id     | some_date      | qty   | price
        3      | 2017-01-06     | 1     | 4.50
    }

    def run2() {
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
        id | some_date | qty | price
        1 | '2017-01-05' | 2 | 1.99

        SqlAssistClassUsage.metaClass.static.dynamicMethod = {
            println "dynamic"
        }
    }
}
