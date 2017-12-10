package info.akang.groovy.transform

import groovy.sql.Sql

class DBUtil {

    static dropAllTables(Sql sql) {
        def rows = sql.rows("select table_name from information_schema.tables where table_schema='PUBLIC'")
        rows.each { row ->
            sql.execute("drop table ${row.table_name}".toString())
        }
    }
}
