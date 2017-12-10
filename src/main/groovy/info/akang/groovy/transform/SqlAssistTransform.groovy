package info.akang.groovy.transform

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Transformation to replace sqlassist DSL with Groovy Sql execution statements.
 * Currently supporting insert statements, to make an easily readable insertion of data to a table.
 * Example:
 *
 * <code>
 * // SqlAssist annotation can be used at class level or method level
 * @SqlAssist
 * class Test {
 *      Sql sqlInstance = Sql.newInstance("jdbc:h2:mem:test", "", "", "org.h2.Driver")
 *
 *      public someMethod() {
 *              insert web_past_purc with sql
 *              user_id     | dlv_dt          | prod_id   | it_qy     | it_pr_qy
 *              1           | '2017-01-05'    | 411       | 2         | 1.99
 *              1           | '2017-01-05'    | 425       | 5         | 19.99
 *      }
 * }
 * </code>
 *
 * someMethod() will be replaced at compile time with:
 * <code>
 *     public someMethod() {
 *         sqlInstance.execute "insert into web_past_purc (user_id,dlv_dt,prod_id,it_qy,it_pr_qy) values ('1','2017-01-05','411','2','1.99')"
 *         sqlInstance.execute "insert into web_past_purc (user_id,dlv_dt,prod_id,it_qy,it_pr_qy) values ('1','2017-01-05','425','5','19.99')"
 *     }
 * </code>
 *
 * The transformation specifically detects the syntax as the start of the sql insertion block:
 * insert <tablename> with <name of Sql object>
 *     as  MethodCallExpression (insert) - ConstantExpression (tablename) - MethodCallExpression (with) - ConstantExpression (name of Sql object)
 *
 */
@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
class SqlAssistTransform extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        nodes.each { new ASTDebugUtil().debug it }

        nodes.each { node ->
            if (node instanceof ClassNode) {
                node.methods.findAll { methodNode ->
                    processStatement(methodNode.code)
                }
            } else if (node instanceof MethodNode) {
                processStatement(node.code)
            }
        }
    }

    /**
     * Process a code block's statements, and perform compile-time replacement if certain sql patterns are found.
     *
     * @param statement
     */
    void processStatement(Statement statement) {
        if (statement instanceof BlockStatement) {
            statement = statement as BlockStatement
            processStatements(statement.statements)
        } else if (statement instanceof TryCatchStatement) {
            statement = statement as TryCatchStatement
            processStatement(statement.tryStatement)
        } else if (statement instanceof IfStatement) {
            statement = statement as IfStatement
            processStatement(statement.ifBlock)

            if (statement.elseBlock) {
                processStatement(statement.elseBlock)
            }
        }
    }

    void processStatements(List<Statement> statements) {
        boolean inInsertBlock = false

        List<SqlAssistReplacement> replacements = []
        SqlAssistReplacement replacement

        for (int i = 0; i < statements.size(); i++) {
            Statement stmt = statements[i]
            processStatement(stmt)

            // Detection of entry of insert block
            if (! inInsertBlock && isInsertEntryPoint(stmt)) {
                inInsertBlock = true
                replacement = new SqlAssistReplacement(stmtIndexStart: i,
                        sql: stmt.expression.arguments.expressions[0].variable,
                        tablename: stmt.expression.objectExpression.arguments.expressions[0].variable)
            }
            // Within insert block
            else if (inInsertBlock) {

                // Detection of exit of insert block
                if (isInsertExitPoint(stmt)) {
                    replacement.stmtIndexEnd = i
                    replacements << generateReplacementInsertAST(replacement)

                    if (isInsertEntryPoint(stmt)) {
                        replacement = new SqlAssistReplacement(stmtIndexStart: i,
                                sql: stmt.expression.arguments.expressions[0].variable,
                                tablename: stmt.expression.objectExpression.arguments.expressions[0].variable)
                    } else {
                        inInsertBlock = false
                    }
                }
                // If not exit, then process values as either header or as a data line
                else {
                    List<String> line = getValues(stmt.expression)
                    replacement.colHeaderLineProcessed ? replacement.addDataLine(line) : replacement.addColumnHeaderLine(line)
                }

            }

        }

        // This is to capture scenario if the insert block is found at the end of the method
        if (inInsertBlock) {
            replacement.stmtIndexEnd = statements.size()
            replacements << generateReplacementInsertAST(replacement)
        }

        replaceStatements(statements, replacements)
    }

    /**
     * Determines if the statement is an entry point to an insert block.
     *
     * @param statement
     * @return
     */
    boolean isInsertEntryPoint(Statement statement) {
        boolean isEntry =
                statement.metaClass.hasProperty(statement, 'expression') &&
                statement.expression instanceof MethodCallExpression &&
                statement.expression.method instanceof ConstantExpression &&
                statement.expression.method.value == "with" &&
                statement.expression.objectExpression instanceof MethodCallExpression &&
                statement.expression.objectExpression.method instanceof ConstantExpression &&
                statement.expression.objectExpression.method.value == "insert"

        return isEntry
    }

    /**
     * Checking if statement is not a data point anymore
     * Note: using getClass().equals instead of instance of because there are some child classes of BinaryExpression that we want
     *   to detect as an exit point such as DeclarationExpression
     *
     * @param statement
     * @return
     */
    boolean isInsertExitPoint(Statement statement) {
        boolean isExit =
                (! (statement.metaClass.hasProperty(statement, 'expression'))) ||
                (! (statement.expression.getClass().equals(BinaryExpression.class))) ||
                (statement.expression.operation.text != "|")

        return isExit
    }

    /**
     * Create replacement insert AST and add to replacement object.
     *
     * @param replacement
     * @return replacement that was passed in and mutated
     */
    SqlAssistReplacement generateReplacementInsertAST(SqlAssistReplacement replacement) {
        replacement.replacementStatements = replacement.dataLines.collect { dataLine ->
            return createInsertAST(replacement.sql, replacement.tablename, replacement.columnHeaders, dataLine)
        }

        return replacement
    }

    /**
     * Replace the DSL statements with the Sql code to insert rows
     *
     * @param statements
     * @param replacements
     * @return
     */
    void replaceStatements(List<Statement> statements, List<SqlAssistReplacement> replacements) {
        replacements.reverse().each { replacement ->
            for (int i = replacement.stmtIndexEnd - 1; i >= replacement.stmtIndexStart; i--) {
                statements.remove(i)
            }

            statements.addAll(replacement.stmtIndexStart, replacement.replacementStatements)
        }
    }

    /**
     * Create one insert Sql Statement code based on sql, tablename, columnHeaders, and data row
     *
     * @param sql
     * @param tablename
     * @param columnHeaders
     * @param data
     * @return
     */
    Statement createInsertAST(String sql, String tablename, List<String> columnHeaders, List<String> data) {
        def insertQuery = "insert into ${tablename} (${columnHeaders.join(',')}) values ('${data.join('\',\'')}')"

        Statement stmt = new ExpressionStatement(
                new MethodCallExpression(
                        new VariableExpression(sql),
                        new ConstantExpression("execute"),
                        new ArgumentListExpression(new ConstantExpression(insertQuery.toString()))
                )
        )

        return stmt
    }

    /**
     * Get values of a Binary expression (e.g. A | B | C) as a List of Strings (e.g. ['A', 'B', 'C'])
     *
     * @param expression
     * @return
     */
    List<String> getValues(Expression expression) {
        List<String> values = []

        while (expression instanceof BinaryExpression) {
            String val = getValue(expression.rightExpression)
            values.add(0, val)
            expression = expression.leftExpression
        }

        values.add(0, getValue(expression))
        return values
    }

    /**
     * Get a single value from an expression
     *
     * @param expression
     * @return
     */
    String getValue(Expression expression) {
        if (expression instanceof VariableExpression) {
            return expression.variable
        } else if (expression instanceof ConstantExpression) {
            return expression.value
        } else if (expression instanceof BinaryExpression) {
            return "${getValue(expression.leftExpression)}${expression.operation.text}${getValue(expression.rightExpression)}"
        }
    }

}




