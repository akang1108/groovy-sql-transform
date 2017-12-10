package info.akang.groovy.transform

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement

class ASTDebugUtil {

    String debug(ASTNode node, int level = 0, boolean print = true) {
        switch (node) {
            case ClassNode:
                return debugClassNode(node as ClassNode, level)
            case FieldNode:
                return debugFieldNode(node as FieldNode, level)
            case MethodNode:
                return debugMethodNode(node as MethodNode, level)
            case PropertyNode:
                return debugPropertyNode(node as PropertyNode, level)

            case BlockStatement:
                return debugBlockStatement(node as BlockStatement, level)
            case ExpressionStatement:
                return debugExpressionStatement(node as ExpressionStatement, level)
            case IfStatement:
                return debugIfStatement(node as IfStatement, level)
            case TryCatchStatement:
                return debugTryCatchStatement(node as TryCatchStatement, level)
            case EmptyStatement:
                return debugEmptyStatement(node as EmptyStatement, level)
            case Statement:
                return debugStatement(node as Statement, level)

            case VariableExpression:
                return debugVariableExpression(node as VariableExpression, level, print)
            case ConstantExpression:
                return debugConstantExpression(node as ConstantExpression, level, print)
            case MethodCallExpression:
                return debugMethodCallExpression(node as MethodCallExpression, level, print)
            case ConstructorCallExpression:
                return debugConstructorCallExpression(node as ConstructorCallExpression, level, print)
            case DeclarationExpression:
                return debugDeclarationExpression(node as DeclarationExpression, level, print)
            case BinaryExpression:
                return debugBinaryExpression(node as BinaryExpression, level, print)
            case BooleanExpression:
                return debugBooleanExpression(node as BooleanExpression, level, print)
            case Expression:
                return debugExpression(node as Expression, level, print)

            default:
                return 'NO MATCH'
        }
    }

    def pre = { ASTNode node, int level ->
        "${"${node.lineNumber}:".padRight(3)} ${'  ' * level}[${node.class.simpleName}]"
    }

    def debugClassNode = { ClassNode node, int level ->
        println "${pre(node, level)} class ${node.name}"
        level++
        node.methods.each { debug(it, level) }
    }

    def debugFieldNode = { FieldNode node, int level ->
        println "${pre(node, level)} field node -----"
    }

    def debugMethodNode = { MethodNode node, int level ->
        println "${pre(node, level)} def ${node.name}"
        level++
        debug(node.code, level)
    }

    def debugPropertyNode = { PropertyNode node, int level ->
        println "${pre(node, level)} property node -----"
    }

    def debugBlockStatement = { BlockStatement node, int level ->
        println "${pre(node, level)}"
        level++
        node.statements.each { debug it, level }
    }

    def debugIfStatement = { IfStatement node, int level ->
        println "${pre(node, level)} if ${debug(node.booleanExpression, level, false)}"
        level++
        debug(node.ifBlock, level)
        if (node.elseBlock) {
            level--
            println "${pre(node, level)} else"
            level++
            debug(node.elseBlock, level)
        }
    }

    def debugTryCatchStatement = { TryCatchStatement node, int level ->
        println "${pre(node, level)} try"
        level++
        debug(node.tryStatement, level)
        level--
        println "${pre(node, level)} catch"
        level++
        node.catchStatements.each { debug (it, level) }
    }

    def debugEmptyStatement = { EmptyStatement node, int level ->
        //println "${pre(node, level)} ${node.text}"
    }

    def debugExpressionStatement = { ExpressionStatement node, int level ->
        debug node.expression, level
    }

    def commonExpression = { ASTNode node, int level, boolean print, String text ->
        if (print) {
            println "${pre(node, level)} $text"
        }

        text
    }

    def debugVariableExpression = { VariableExpression node, int level, boolean print = true ->
        String text = "${node.type.nameWithoutPackage} ${node.name}"
        commonExpression node, level, print, text
    }

    def debugConstantExpression = { ConstantExpression node, int level, boolean print = true ->
        String text = "${node.text}"
        commonExpression node, level, print, text
    }

    def debugMethodCallExpression = { MethodCallExpression node, int level, boolean print = true ->
        String text = "${node.text}"
        commonExpression node, level, print, text
    }

    def debugConstructorCallExpression = { ConstructorCallExpression node, int level, boolean print = true ->
        String text = "${node.methodAsString} ${node.text}"
        commonExpression node, level, print, text
    }

    def debugDeclarationExpression = { DeclarationExpression node, int level, boolean print = true ->
        String text = "${debug(node.leftExpression, level, false)} ${node.operation.text} ${debug(node.rightExpression, level, false)}"
        commonExpression node, level, print, text
    }

    def debugBinaryExpression = { BinaryExpression node, int level, boolean print = true ->
        String text = "${debug(node.leftExpression, level, false)} ${node.operation.text} ${debug(node.rightExpression, level, false)}"
        commonExpression node, level, print, text
    }

    def debugBooleanExpression = { BooleanExpression node, int level, boolean print = true ->
        String text = "${node.text}"
        commonExpression node, level, print, text
    }

    def debugStatement = { Statement node, int level ->
        println "${pre(node, level)} IMPLEMENT"
    }

    def debugExpression = { Expression node, int level, boolean print = false ->
        String text = "IMPLEMENT"
        String returnText = commonExpression node, level, print, text
        "${node.class.simpleName} ${returnText}"
    }
}
