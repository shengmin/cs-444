package joos.ast

import joos.ast.expressions.NameExpression
import joos.parsetree.ParseTreeNode

case class SimpleType(name: NameExpression) extends Type {
  override def asName = name
}

object SimpleType {
  // TODO: Change it to ClassOrInterfaceType??
  def apply(ptn: ParseTreeNode): SimpleType = SimpleType(NameExpression(ptn))
}
