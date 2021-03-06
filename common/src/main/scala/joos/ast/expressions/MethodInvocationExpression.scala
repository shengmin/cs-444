package joos.ast.expressions

import joos.ast.declarations.MethodDeclaration
import joos.ast.{DeclarationReference, AstConstructionException}
import joos.syntax.language.ProductionRule
import joos.syntax.parsetree.{TreeNode, ParseTreeNode}

case class MethodInvocationExpression(
    expression: Option[Expression],
    methodName: NameExpression,
    arguments: IndexedSeq[Expression]) extends Expression with DeclarationReference[MethodDeclaration] {

  override def toString = {
    val prefix = expression match {
      case None => ""
      case Some(expression) => expression.toString + '.'
    }
    s"${prefix}${methodName}(${arguments.mkString(", ")})"
  }
}

object MethodInvocationExpression {
  def apply(ptn: ParseTreeNode): MethodInvocationExpression = {
    ptn match {
      case TreeNode(ProductionRule("MethodInvocation", Seq("Name", "(", ")")), _, children) =>
        MethodInvocationExpression(None, NameExpression(children(0)), IndexedSeq())
      case TreeNode(ProductionRule("MethodInvocation", Seq("Name", "(", "ArgumentList", ")")), _, children) =>
        MethodInvocationExpression(None, NameExpression(children(0)), Expression.argList(children(2)))
      case TreeNode(ProductionRule("MethodInvocation", Seq("Primary", ".", "Identifier", "(", ")")), _, children) =>
        MethodInvocationExpression(Some(Expression(children(0))), SimpleNameExpression(children(2)), IndexedSeq())
      case TreeNode(ProductionRule("MethodInvocation", Seq("Primary", ".", "Identifier", "(", "ArgumentList", ")")), _, children) =>
        MethodInvocationExpression(Some(Expression(children(0))), SimpleNameExpression(children(2)), Expression.argList(children(4)))
      case _ => throw new AstConstructionException("No valid production rule to make MethodInvocationExpression")
    }
  }
}
