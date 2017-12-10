package info.akang.groovy.transform

class SqlAssistSpockTest extends GroovyTestCase {

    void testSqlAssistTransform() {

        new GroovyShell(getClass().classLoader).evaluate '''

import info.akang.groovy.transform.SqlAssist
import info.akang.groovy.transform.DBUtil
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.junit.runner.JUnitCore
import org.junit.runner.Result
import spock.lang.Specification

@SqlAssist
class Testing extends Specification {

    static void main(String[] args) {
         Result result = JUnitCore.runClasses Testing
         if (result.failures.size() > 0) {
             println "failures: ${result.failures}"
             assert false
         }
    }
    
    def "some test"() {
        given:
        Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")
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
       
        when: 
        List<GroovyRowResult> rows = sql.rows("select * from test1")
        println rows
        
        then:
        rows.size() == 2
    }
    
}

'''

    }


}
