package joos.semantic

import joos.ast.CompilationUnit
import joos.ast.declarations.{PackageDeclaration, ImportDeclaration, TypeDeclaration}
import joos.ast.expressions.{QualifiedNameExpression, SimpleNameExpression, NameExpression}
import joos.core.Logger

trait CompilationUnitEnvironment extends Environment {
  self: CompilationUnit =>

  val concreteImports = new NamespaceTrie
  val onDemandImports = new NamespaceTrie

  private def getTypeFromOnDemandImports(typeName: SimpleNameExpression) = {
    onDemandImports.getSimpleType(typeName)
  }

  private def addOnDemandImport(name: NameExpression) {
    moduleDeclaration.namespace.getAllTypesInPackage(name) map {
      typeDeclaration =>
        onDemandImports.add(name, Some(typeDeclaration))
    }
  }

  private def getTypeFromConcreteImports(name: SimpleNameExpression) = concreteImports.getSimpleType(name)

  private def addConcreteImport(packageName: NameExpression, typeDeclaration: TypeDeclaration) {
    val qualifiedName = QualifiedNameExpression(packageName, typeDeclaration.name)
    concreteImports.getQualifiedType(qualifiedName) match {
      case None => {
        getTypeFromConcreteImports(typeDeclaration.name) match {
          case None => concreteImports.add(packageName, Some(typeDeclaration))
          case _ => throw new NamespaceCollisionException(qualifiedName)
        }
      }
      case _ => Logger.logInformation(s"Attempting to add duplicate concrete import ${typeDeclaration.name }")
    }
  }

  def checkDuplicates(onDemandType: Option[TypeDeclaration], concreteType: Option[TypeDeclaration]) = {
    if (onDemandType.isDefined && concreteType.isDefined) {
      if (!(onDemandType.get eq concreteType.get)) {
        val typeName = onDemandType.get.name
        Logger.logError(s"On-demand import and concrete import conflicted for ${typeName}")
        throw new NamespaceCollisionException(typeName)
      }
    }
    (onDemandType ++ concreteType).headOption
  }

  /**
   * Gets the type with the {{name}} if it's visible within this compilation unit
   */
  def getVisibleType(name: NameExpression): Option[TypeDeclaration] = {
    name match {
      case simpleName: SimpleNameExpression => {
       checkDuplicates(getTypeFromOnDemandImports(simpleName), getTypeFromConcreteImports(simpleName))
      }
      case qualifiedName: QualifiedNameExpression => moduleDeclaration.namespace.getQualifiedType(qualifiedName)
    }
  }

  def add(importDeclaration: ImportDeclaration): this.type = {
    if (importDeclaration.isOnDemand) {
      addOnDemandImport(importDeclaration.name)
    } else {
      importDeclaration.name match {
        case e@QualifiedNameExpression(qualifier, typeName) => {
          moduleDeclaration.namespace.getQualifiedType(e) match {
            case Some(typeDeclaration) => addConcreteImport(qualifier, typeDeclaration)
            case _ => throw new MissingTypeException(importDeclaration.name)
          }
        }
        case _ => {
          Logger.logError(s"ImportDeclaration was given a SimpleNameExpression ${importDeclaration.name}")
          throw new RuntimeException("This also shouldnt happen")
        }
      }
    }
    this
  }

  def addDefaultAndSelf(): this.type = {
    val defaultPackageName = PackageDeclaration.DefaultPackage.name

    for (packageToImport <- Set(defaultPackageName, packageDeclaration.name)) {
      moduleDeclaration.namespace.getAllTypesInPackage(packageToImport) foreach {
        typeDeclaration =>
          addConcreteImport(defaultPackageName, typeDeclaration)
      }
    }
    this
  }
}
