package joos.ast.expressions

import joos.ast.AstConstructionException
import joos.ast.Modifier
import joos.ast.compositions.{BlockLike, TypedDeclarationLike}
import joos.ast.declarations.VariableDeclarationFragment
import joos.ast.types.Type
import joos.syntax.language.ProductionRule
import joos.syntax.parsetree.{TreeNode, ParseTreeNode}

case class VariableDeclarationExpression(
    modifiers: Seq[Modifier],
    variableType: Type,
    fragment: VariableDeclarationFragment) extends Expression with TypedDeclarationLike with BlockLike {
  override def declarationName = fragment.identifier

  override def declarationType = variableType

  override def toString = {
    s"${variableType.standardName} ${modifiers.mkString(" ")} ${fragment}"
  }
}

object VariableDeclarationExpression {
  def apply(ptn: ParseTreeNode): VariableDeclarationExpression = {
    ptn match {
      case TreeNode(ProductionRule("LocalVariableDeclaration", Seq("Type", "VariableDeclarator")), _, children) =>
        VariableDeclarationExpression(Seq.empty, Type(children(0)), VariableDeclarationFragment(children(1)))
      case _ => throw new AstConstructionException("No production rule to create VariableDeclarationExpression")
    }
  }
}
