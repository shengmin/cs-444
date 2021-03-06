package joos.ast

import joos.ast.declarations.{ModuleDeclaration, TypeDeclaration, PackageDeclaration, ImportDeclaration}
import joos.syntax.language.ProductionRule
import joos.syntax.parsetree.{TreeNode, ParseTreeNode}
import joos.semantic.CompilationUnitEnvironment

case class CompilationUnit(
    packageDeclaration: PackageDeclaration,
    importDeclarations: Seq[ImportDeclaration],
    typeDeclaration: Option[TypeDeclaration])
    extends AstNode
    with CompilationUnitEnvironment {
  var moduleDeclaration: ModuleDeclaration = null
}

object CompilationUnit {
  def apply(ptn: ParseTreeNode): CompilationUnit = {
    ptn match {
      case TreeNode(ProductionRule("CompilationUnit", Seq()), _, children) =>
        CompilationUnit(PackageDeclaration.DefaultPackage, Seq.empty, None)
      case TreeNode(ProductionRule("CompilationUnit", Seq("TypeDeclaration")), _, children) =>
        CompilationUnit(PackageDeclaration.DefaultPackage, Seq.empty, Some(TypeDeclaration(children(0))))
      case TreeNode(ProductionRule("CompilationUnit", Seq("ImportDeclarations")), _, children) =>
        CompilationUnit(PackageDeclaration.DefaultPackage, ImportDeclaration(children(0)), None)
      case TreeNode(ProductionRule("CompilationUnit", Seq("ImportDeclarations", "TypeDeclaration")), _, children) =>
        CompilationUnit(
          PackageDeclaration.DefaultPackage,
          ImportDeclaration(children(0)),
          Some(TypeDeclaration(children(1))))
      case TreeNode(ProductionRule("CompilationUnit", Seq("PackageDeclaration")), _, children) =>
        CompilationUnit(PackageDeclaration(children(0)), Seq.empty, None)
      case TreeNode(ProductionRule("CompilationUnit", Seq("PackageDeclaration", "TypeDeclaration")), _, children) =>
        CompilationUnit(PackageDeclaration(children(0)), Seq.empty, Some(TypeDeclaration(children(1))))
      case TreeNode(ProductionRule("CompilationUnit", Seq("PackageDeclaration", "ImportDeclarations")), _, children) =>
        CompilationUnit(PackageDeclaration(children(0)), ImportDeclaration(children(1)), None)
      case TreeNode(
      ProductionRule(
      "CompilationUnit",
      Seq("PackageDeclaration", "ImportDeclarations", "TypeDeclaration")), _, children) =>
        CompilationUnit(
          PackageDeclaration(children(0)),
          ImportDeclaration(children(1)),
          Some(TypeDeclaration(children(2))))
      case _ => throw new AstConstructionException(
        "No valid production rule to create CompilationUnit"
      )
    }
  }
}
