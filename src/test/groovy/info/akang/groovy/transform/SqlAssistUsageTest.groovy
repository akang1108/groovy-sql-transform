package info.akang.groovy.transform

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TransformTestHelper

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SqlAssistUsageTest extends GroovyTestCase {

    Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")

    void testClassUsage() {
        def file = new File('src/test/groovy/info/akang/groovy/transform/SqlAssistClassUsage.groovy')
        assert file.exists()

        def invoker = new TransformTestHelper(new SqlAssistTransform(), CompilePhase.SEMANTIC_ANALYSIS)
        def usage = invoker.parse(file).newInstance()

        usage.run()

        List<GroovyRowResult> table1Rows = sql.rows("select * from test1 order by id asc")
        assert table1Rows.size() == 3
        compareTest1Row(table1Rows[0], [ id: 1, some_date: '2017-01-05', qty: 2, price: 1.99 ])
        compareTest1Row(table1Rows[1], [ id: 2, some_date: '2017-01-05', qty: 5, price: 3.99 ])
        compareTest1Row(table1Rows[2], [ id: 3, some_date: '2017-01-06', qty: 1, price: 4.50 ])

        usage.run2()
    }

    void testMethodUsage() {
        def file = new File('src/test/groovy/info/akang/groovy/transform/SqlAssistMethodUsage.groovy')
        assert file.exists()

        def invoker = new TransformTestHelper(new SqlAssistTransform(), CompilePhase.SEMANTIC_ANALYSIS)
        def usage = invoker.parse(file).newInstance()

        usage.run()

        List<GroovyRowResult> table1Rows = sql.rows("select * from test1 order by id asc")
        assert table1Rows.size() == 1
        compareTest1Row(table1Rows[0], [ id: 1, some_date: '2017-01-05', qty: 2, price: 1.99 ])
    }

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
}
