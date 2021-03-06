package joos.codegen.generators

import joos.assemgen.AssemblyLine
import joos.ast.AbstractSyntaxTreeDispatcher
import joos.ast.declarations._
import joos.ast.expressions._
import joos.ast.statements._
import joos.ast.{AbstractSyntaxTree, AstNode}
import joos.codegen.AssemblyCodeGeneratorEnvironment
import scala.Some
import scala.language.implicitConversions

trait AssemblyCodeGenerator {
  def generate()

  def environment: AssemblyCodeGeneratorEnvironment

  def appendText(lines: AssemblyLine*) {
    environment.assemblyManager.appendText(lines: _*)
  }

  def appendGlobal(lines: String*) {
    environment.assemblyManager.appendGlobal(lines: _*)
    environment.namespace.externs ++= lines
  }

  def appendData(lines: AssemblyLine*) {
    environment.assemblyManager.appendData(lines: _*)
  }

  implicit protected def toAssemblyCodeGenerator(node: AstNode)
      (implicit environment: AssemblyCodeGeneratorEnvironment): AssemblyCodeGenerator = {
    var generator: AssemblyCodeGenerator = null
    val creator = new AbstractSyntaxTreeDispatcher {

      override def apply(field: FieldDeclaration) = generator = new FieldDeclarationCodeGenerator(field)

      override def apply(method: MethodDeclaration) = generator = new MethodDeclarationCodeGenerator(method)

      override def apply(variable: SingleVariableDeclaration) = generator = new SingleVarDeclarationCodeGenerator(variable)

      override def apply(statement: ExpressionStatement) = generator = new ExpressionStatementCodeGenerator(statement)

      override def apply(tipe: TypeDeclaration) = generator = new TypeDeclarationCodeGenerator(tipe)

      override def apply(statement: ForStatement) = generator = new ForStatementCodeGenerator(statement)

      override def apply(statement: IfStatement) = generator = new IfStatementCodeGenerator(statement)

      override def apply(statement: ReturnStatement) = generator = new ReturnStatementCodeGenerator(statement)

      override def apply(statement: WhileStatement) = generator = new WhileStatementCodeGenerator(statement)

      override def apply(statement: Block) = generator = new BlockCodeGenerator(statement)

      override def apply(statement: EmptyStatement.type) = generator = new EmptyStatementCodeGenerator(statement)

      override def apply(expression: ArrayAccessExpression) = generator = new ArrayAccessExpressionCodeGenerator(expression)

      override def apply(expression: ArrayCreationExpression) = generator = new ArrayCreationExpressionCodeGenerator(expression)

      override def apply(expression: AssignmentExpression) = generator = new AssignmentExpressionCodeGenerator(expression)

      override def apply(expression: BooleanLiteral) = generator = new BooleanLiteralCodeGenerator(expression)

      override def apply(expression: CastExpression) = generator = new CastExpressionCodeGenerator(expression)

      override def apply(expression: CharacterLiteral) = generator = new CharacterLiteralCodeGenerator(expression)

      override def apply(expression: IntegerLiteral) = generator = new IntegerLiteralCodeGenerator(expression)

      override def apply(expression: StringLiteral) = generator = new StringLiteralCodeGenerator(expression)

      override def apply(expression: NullLiteral) = generator = new NullLiteralCodeGenerator(expression)

      override def apply(expression: ClassInstanceCreationExpression) = generator = new ClassInstanceCreationExpressionCodeGenerator(expression)

      override def apply(expression: FieldAccessExpression) = generator = new FieldAccessExpressionCodeGenerator(expression)

      override def apply(expression: InfixExpression) = generator = new InfixExpressionCodeGenerator(expression)

      override def apply(expression: InstanceOfExpression) = generator = new InstanceOfExpressionCodeGenerator(expression)

      override def apply(expression: MethodInvocationExpression) = generator = new MethodInvocationExpressionCodeGenerator(expression)

      override def apply(expression: PrefixExpression) = generator = new PrefixExpressionCodeGenerator(expression)

      override def apply(expression: VariableDeclarationExpression) = generator = new VariableDeclarationExpressionCodeGenerator(expression)

      override def apply(fragment: VariableDeclarationFragment) = generator = new VariableDeclarationFragmentCodeGenerator(fragment)

      override def apply(expression: ThisExpression) = generator = new ThisExpressionCodeGenerator(expression)

      override def apply(expression: SimpleNameExpression) = generator = new SimpleNameExpressionCodeGenerator(expression)

      override def apply(expression: QualifiedNameExpression) = generator = new QualifiedNameExpressionCodeGenerator(expression)
    }

    creator(node)
    assert(generator != null)
    generator
  }
}

object AssemblyCodeGenerator {
  def apply(ast: AbstractSyntaxTree)(implicit environment: AssemblyCodeGeneratorEnvironment) {
    ast.root.typeDeclaration match {
      case None =>
      case Some(typeDeclaration) => new TypeDeclarationCodeGenerator(typeDeclaration).generate
    }
  }
}

