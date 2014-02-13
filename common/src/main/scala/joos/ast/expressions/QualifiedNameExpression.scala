package joos.ast.expressions

import joos.ast.exceptions.AstConstructionException
import joos.language.ProductionRule
import joos.parsetree.{TreeNode, ParseTreeNode}

case class QualifiedNameExpression(qualifier: NameExpression, name: SimpleNameExpression) extends NameExpression


object QualifiedNameExpression {
  def apply(ptn: ParseTreeNode): QualifiedNameExpression = {
    ptn match {
      case TreeNode(ProductionRule("QualifiedName", _), _, children) =>
        return QualifiedNameExpression(NameExpression(children(0)), SimpleNameExpression(children(2)))
      case _ => throw new AstConstructionException("No valid production rule to make QualifiedNameExpression")
    }
  }
}
