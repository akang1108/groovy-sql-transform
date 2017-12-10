package info.akang.groovy.transform

class SqlAssistTest extends GroovyTestCase {

    void testSqlAssistTransform() {

        new GroovyShell(getClass().classLoader).evaluate '''

import info.akang.groovy.transform.SqlAssist
import info.akang.groovy.transform.DBUtil
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

@SqlAssist
class Testing {

    Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")

    static void main(String[] args) {
        Testing testing = newInstance()
        testing.run()
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
        3      | '2017-01-06'   | 1     | 4.50

        sql.execute """
            CREATE TABLE test2
            (
              id INTEGER,
              name VARCHAR(100)
            );"""

        insert test2 with sql
        id  | name
        1   | 'batman'
        
        def msEpoch = { String strDate ->
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return java.sql.Date.valueOf(LocalDate.parse(strDate, formatter)).getTime()
        }
        
        def compareTest1Row = { GroovyRowResult row, Map m ->
            assert row.id == m.id
            assert row.some_date.getTime() == msEpoch(m.some_date)
            assert row.qty == m.qty
            assert row.price == m.price
        }

        List<GroovyRowResult> table1Rows = sql.rows("select * from test1 order by id asc")
        assert table1Rows.size() == 3
        compareTest1Row(table1Rows[0], [ id: 1, some_date: '2017-01-05', qty: 2, price: 1.99 ])
        compareTest1Row(table1Rows[1], [ id: 2, some_date: '2017-01-05', qty: 5, price: 3.99 ])
        compareTest1Row(table1Rows[2], [ id: 3, some_date: '2017-01-06', qty: 1, price: 4.50 ])

        List<GroovyRowResult> table2Rows = sql.rows("select * from test2 order by id asc")
        assert table2Rows.size() == 1
        assert table2Rows[0].id == 1
        assert table2Rows[0].name == 'batman'
        
    }

}

'''

    }


}
