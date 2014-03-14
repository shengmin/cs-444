package joos.ast.expressions

import joos.ast.AstConstructionException
import joos.syntax.parsetree.{LeafNode, ParseTreeNode}
import joos.syntax.tokens.{TokenKind, TerminalToken}
import joos.ast.types.PrimitiveType._

case class NullLiteral(token: TerminalToken) extends LiteralExpression {
  override def declarationType = NullType
}

object NullLiteral {
  def apply(ptn: ParseTreeNode): NullLiteral = {
    ptn match {
      case LeafNode(token) if token.kind == TokenKind.NullLiteral =>
        NullLiteral(token)
      case _ => throw new AstConstructionException("No valid production rule to make NullLiteral")
    }
  }
}
