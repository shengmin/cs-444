package joos.codegen.generators

import joos.ast.declarations.VariableDeclarationFragment
import joos.codegen.AssemblyFileManager
import joos.assemgen.Register._
import joos.assemgen._
import joos.codegen.AssemblyCodeGeneratorEnvironment

class VariableDeclarationFragmentCodeGenerator(expression: VariableDeclarationFragment)
    (implicit val environment: AssemblyCodeGeneratorEnvironment) extends AssemblyCodeGenerator {

  override def generate() {
    require(expression.initializer.isDefined)
    expression.initializer.foreach(_.generate())
  }

}
