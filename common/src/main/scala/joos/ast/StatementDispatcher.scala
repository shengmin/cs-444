package joos.ast

import joos.ast.statements._

/**
 * Dispatches to a more specific [[Statement]]
 */
trait StatementDispatcher {
  protected def dispatchStatement(statement: Statement) = this(statement)

  def apply(statement: Statement) {
    statement match {
      case node: Block => this(node)
      case node: ExpressionStatement => this(node)
      case node: ForStatement => this(node)
      case node: IfStatement => this(node)
      case node: ReturnStatement => this(node)
      case node: WhileStatement => this(node)
      case node: EmptyStatement.type => this(node)
    }
  }

  def apply(statement: ExpressionStatement) {}

  def apply(statement: ForStatement) {}

  def apply(statement: IfStatement) {}

  def apply(statement: ReturnStatement) {}

  def apply(statement: WhileStatement) {}

  def apply(statement: Block) {
    statement.statements.foreach(apply)
  }

  def apply(statement: EmptyStatement.type) {}
}
