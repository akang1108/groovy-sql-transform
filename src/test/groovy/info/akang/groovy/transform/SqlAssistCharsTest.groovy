package info.akang.groovy.transform

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TransformTestHelper

class SqlAssistCharsTest extends GroovyTestCase {

    Sql sql = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")

    void testChars() {
        def file = new File('src/test/groovy/info/akang/groovy/transform/SqlAssistChars.groovy')
        assert file.exists()

        def invoker = new TransformTestHelper(new SqlAssistTransform(), CompilePhase.SEMANTIC_ANALYSIS)
        def usage = invoker.parse(file).newInstance()

        usage.run()

        List<GroovyRowResult> rows = sql.rows("select * from test_table order by id asc")
        assert rows.size() == 1
//        compareRow(rows[0], [ id: 1, some_text: 'abcde'])

        println rows
    }

    def compareRow = { GroovyRowResult row, Map m ->
        assert row.id == m.id
        assert row.some_text == m.some_text
    }
}
