package cypharse

import org.neo4j.cypher.internal.frontend.v3_0.parser.CypherParser
import org.neo4j.cypher.internal.frontend.v3_0.ast._
import org.neo4j.cypher.internal.frontend.v3_0.{SemanticState, SemanticCheckResult}

import scala.util.{Try, Success, Failure}
import scala.collection.mutable

sealed trait BoundVariable {
  val variable: Variable
}

case class BoundRelationVar(variable: Variable, labels: Seq[RelTypeName]) extends BoundVariable
case class BoundNodeVar(variable: Variable, labels: Seq[LabelName]) extends BoundVariable

object CypherChecker {
  private def boundVariablesFromElement(element: PatternElement): Seq[BoundVariable] = element match {
    case NodePattern(Some(variable), labels, properties) => Seq(BoundNodeVar(variable, labels))
    case NodePattern(None, _, _) => Seq.empty //TODO need to check existence of labels in schema?
    case RelationshipChain(element, relationship, rightNode) => boundVariablesFromElement(element) ++ boundVariablesFromRelationship(relationship) ++ boundVariablesFromElement(rightNode)
  }

  private def boundVariablesFromRelationship(relationship: RelationshipPattern) = {
    relationship.variable.map(v => BoundRelationVar(v, relationship.types)).toSeq
  }

  private def boundVariablesFromPattern(pattern: Pattern) = {
    val elements = pattern.patternParts.map(_.element)
    elements.flatMap(boundVariablesFromElement)
  }

  private def boundVariablesFromPart(part: QueryPart) = part match {
    case SingleQuery(clauses) =>
      val boundVars = clauses collect {
        case Match(optional, pattern, hints, where) => boundVariablesFromPattern(pattern)
        case Merge(pattern, actions) => boundVariablesFromPattern(pattern)
        case Create(pattern) => boundVariablesFromPattern(pattern)
        case CreateUnique(pattern) => boundVariablesFromPattern(pattern)
      }
      boundVars.flatten
  }

  private def returnedVariablesFromPart(part: QueryPart): Either[Seq[String], Seq[BoundVariable]] = {
    val boundVars = boundVariablesFromPart(part)
    val varMap = boundVars.groupBy(_.variable.name)
    val errors = mutable.ArrayBuffer.empty[String]

    val duplicateVars = varMap collect { case (name, _::_::_) => name }
    if (duplicateVars.nonEmpty) //TODO: handle duplicates as additional? matches
      errors += s"""found duplicate variables: ${duplicateVars.mkString(", ")}"""

    // semantic check already handles this, but maybe we are missing some variables in our collection
    val undefinedVars = part.returnColumns.filterNot(varMap.contains)
    if (undefinedVars.nonEmpty)
      errors += s"""found undefined variables in return: ${undefinedVars.mkString(", ")}"""

    if (errors.isEmpty)
      Right(part.returnColumns.map(r => varMap.apply(r).head))
    else Left(errors.toSeq)
  }

  def returns(statement: Statement) = statement match {
    case Query(None, part) => returnedVariablesFromPart(part)
    case _ => Left(List(s"unexpected statement: $statement"))
  }

  private val parser = new CypherParser

  def check(query: String): Either[Seq[String], Statement] = Try(parser.parse(query)) match {
    case Success(statement) => statement.semanticCheck(SemanticState.clean) match {
      case SemanticCheckResult(_, Nil) => Right(statement)
      case SemanticCheckResult(_, errors) => Left(errors.map(e => s"${e.msg} (${e.position})"))
    }
    case Failure(e) => Left(Seq(s"parse error: ${e.getMessage}"))
  }
}
