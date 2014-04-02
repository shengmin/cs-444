package joos.codegen.generators

import joos.assemgen.Register._
import joos.assemgen._
import joos.ast.NameClassification._
import joos.ast.expressions.SimpleNameExpression
import joos.codegen.AssemblyCodeGeneratorEnvironment

class SimpleNameExpressionCodeGenerator(expression: SimpleNameExpression)
    (implicit val environment: AssemblyCodeGeneratorEnvironment) extends AssemblyCodeGenerator {

  override def generate() {

    expression.nameClassification match {

      case LocalVariableName => {
        val slot = environment.getVariableSlot(expression)
        appendText(
          mov(Edx, Ebp) :# "Local Variable access. Move ebp into edx",
          sub(Edx, slot * 4) :# s"Retrieve lvalue address of variable ${expression.standardName}",
          mov(Eax, at(Edx)) :# s"Retrieve variable ${expression.standardName}"
        )
      }

      case InstanceFieldName => {
        val slot = environment.getFieldSlot(expression)
        // Assume Ecx is pointer to this
        appendText(
          mov(Edx, Ecx) :# "Field Access. Move this into edx",
          add(Edx, slot * 4 + FieldOffset) :# s"lvalue of field ${expression.standardName} is at offset ${slot * 4 + FieldOffset}",
          mov(Eax, at(Edx)) :# s"Retrieve field ${expression.standardName}"
        )
      }
    }
  }

}
