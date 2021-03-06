package joos.ast.types

import joos.ast.expressions.NameExpression
import joos.syntax.parsetree.ParseTreeNode

case class SimpleType(name: NameExpression) extends Type {

  override def equals(that: Any) = {
    that match {
      case that: SimpleType => that.declaration == declaration
      case _ => false
    }
  }

  override def standardName = name.standardName
}

object SimpleType {
  def apply(ptn: ParseTreeNode): SimpleType = new SimpleType(NameExpression(ptn))
}
