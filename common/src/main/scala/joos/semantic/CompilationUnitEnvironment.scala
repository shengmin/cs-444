package joos.semantic

import joos.ast.declarations.{ImportDeclaration, TypeDeclaration}
import joos.ast.expressions.NameExpression
import joos.ast.CompilationUnit

trait CompilationUnitEnvironment extends Environment {
  self: CompilationUnit =>

  /**
   * Gets the type with the {{name}} if it's visible within this compilation unit
   */
  def getVisibleType(name: NameExpression): Option[TypeDeclaration] = {
    // TODO: implement
    None
  }

  def add(importDeclaration: ImportDeclaration): this.type = {
    // TODO: implement
    this
  }
}