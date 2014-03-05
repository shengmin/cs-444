package joos.ast.expressions

import joos.ast.AstConstructionException
import joos.syntax.parsetree.{LeafNode, ParseTreeNode}
import joos.syntax.tokens.{TokenKind, TerminalToken}

case class BooleanLiteral(token: TerminalToken) extends LiteralExpression


object BooleanLiteral {
  def apply(ptn: ParseTreeNode): BooleanLiteral = {
    ptn match {
      case LeafNode(token) if token.kind == TokenKind.True || token.kind == TokenKind.False =>
        BooleanLiteral(token)
      case _ => throw new AstConstructionException("No valid production rule to make BooleanLiteral")
    }
  }
}
