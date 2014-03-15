package joos.semantic.types.disambiguation

import joos.ast.compositions.NameLike
import joos.ast.declarations.{MethodDeclaration, FieldDeclaration}
import joos.ast.expressions._
import joos.ast.types.Type
import joos.ast.visitor.AstCompleteVisitor
import joos.ast.{Modifier, CompilationUnit}
import joos.semantic.types.AstEnvironmentVisitor
import joos.semantic.types.disambiguation.Visibility._

// Check the rules specified in Section 8.3.2.3 of the Java Language Specification regarding forward references. The initializer of a non-static
// field must not use (i.e. read) by simple name (i.e. without an explicit this) itself or a non-static field declared later in the same class.
class ForwardUseChecker(fieldScope: Map[SimpleNameExpression, Type]) extends AstCompleteVisitor {

  override def apply(expression: FieldAccessExpression) {
    expression.expression match {
      case t: ThisExpression => return // No uses-before-declaration can occur in a this expression
      case _ => {
        expression.expression.accept(this)
        expression.identifier.accept(this)
      }
    }
  }

  override def apply(fieldName: SimpleNameExpression) {
    if (!(fieldScope contains fieldName)) {
      throw new ForwardFieldUseException(fieldName)
    }
  }

  override def apply(fieldAccess: QualifiedNameExpression) {
    if (fieldAccess.qualifier.classification == NameLike.ExpressionName) {
      fieldAccess.qualifier.accept(this)
    }
  }

  override def apply(assignment: AssignmentExpression) {
    assignment.right.accept(this)

    assignment.left match {
      case name: SimpleNameExpression => return // No uses-before-declaration can occur as a simple name on the left hand side of an assignment
      case complex => complex.accept(this)
    }
  }
}


class StaticNameLinker(implicit unit: CompilationUnit) extends AstEnvironmentVisitor {
  private var localFields = Map.empty[SimpleNameExpression, Type]
  private var visibility = Local
  private var inStaticMethod = false

  override def apply(fieldDeclaration: FieldDeclaration) {
    fieldDeclaration.fragment.initializer foreach (_.accept(new ForwardUseChecker(localFields)))
    if (!(fieldDeclaration.modifiers contains Modifier.Static)) {
      localFields += (fieldDeclaration.declarationName -> fieldDeclaration.declarationType)
    }

    val oldVisibility = visibility
    fieldDeclaration.isStatic match {
      case true => visibility = Static
      case false => visibility = Local
    }
    fieldDeclaration.fragment.initializer foreach (_.accept(this))
    visibility = oldVisibility
  }

  override def apply(methodDeclaration: MethodDeclaration) {
    val oldVisibility = visibility
    inStaticMethod = methodDeclaration.isStatic
    methodDeclaration.isStatic match {
      case true => visibility = Static
      case false => visibility = Local
    }

    super.apply(methodDeclaration)

    visibility = oldVisibility
    inStaticMethod = false
  }

  override def apply(fieldAccess: FieldAccessExpression) {
    fieldAccess.expression.accept(this)
  }

  override def apply(invocation: MethodInvocationExpression) {
    invocation.expression foreach (_.accept(this))
    invocation.arguments foreach (_.accept(this))
  }

  override def apply(name: SimpleNameExpression) {
    var declarationType: Type = null

    // (1) Check local variable
    require(blockEnvironment != null)
    blockEnvironment.getLocalVariable(name) match {
      case Some(localVariable) => {
//        visibility match {
//          case Static => if (!localVariable.modifiers.contains(Modifier.Static)) throw new InvalidStaticUseException(name)
//          case Local => if (localVariable.modifiers.contains(Modifier.Static)) throw new InvalidStaticUseException(name)
//        }
        declarationType = localVariable.declarationType
      }
      case None =>

        if (inStaticMethod) {
          throw new AmbiguousNameException(name)
        }
        // (2) Check local field
        getFieldTypeFromType(unit.typeDeclaration.get.asType, name, visibility) match {
          //        typeEnvironment.containedFields.get(name) match {
          case Some(fieldType) => {
            declarationType = fieldType
          }
          case None => {

            // (3) Check Static access
            unit.getVisibleType(name) match {
              case Some(typeName) => {
                declarationType = typeName.asType
              }
              case None => throw new AmbiguousNameException(name)
            }
          }
        }
    }
    name.declarationType = declarationType
  }

  override def apply(name: QualifiedNameExpression) {
    resolveFieldAccess(name)
  }

}
