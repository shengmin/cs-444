package joos.semantic

import joos.ast.TypedDeclaration
import joos.ast.expressions.{SimpleNameExpression, NameExpression}

/**
 * This class is immutable since it needs to take a snapshot of the environment
 * A later variable declaration in the outer scope should not be visible inside an
 * early inner scope
 */
class BlockEnvironment private(
    val typeEnvironment: TypeEnvironment,
    val variables: Map[NameExpression, TypedDeclaration]) {

  def add(variable: TypedDeclaration): BlockEnvironment = {
    if (variables.contains(variable.declarationName)) {
      throw new DuplicatedDeclarationException(variable.declarationName)
    }
    new BlockEnvironment(typeEnvironment, variables + ((variable.declarationName, variable)))
  }

  def getVariable(name: SimpleNameExpression): Option[TypedDeclaration] = {
    variables.get(name) match {
      case None => typeEnvironment.getField(name)
      case x => x
    }
  }
}

object BlockEnvironment {
  def apply()(implicit typeEnvironment: TypeEnvironment): BlockEnvironment = {
    new BlockEnvironment(typeEnvironment, Map())
  }
}
