package cypharse

import org.neo4j.cypher.internal.frontend.v3_0.parser.CypherParser
import org.neo4j.cypher.internal.frontend.v3_0.ast._

import scala.util.{Try, Success, Failure}
import scala.collection.mutable

trait QueryResult

case class QuerySuccess(boundVariables: Seq[BoundVariable]) extends QueryResult
case class QueryFailure(errors: List[String]) extends QueryResult

sealed trait BoundVariable {
  val variable: Variable
}

case class BoundRelationVar(variable: Variable, labels: Seq[RelTypeName]) extends BoundVariable
case class BoundNodeVar(variable: Variable, labels: Seq[LabelName]) extends BoundVariable

object CypherChecker {
  def boundVariablesFromElement(element: PatternElement): Seq[BoundVariable] = element match {
    case NodePattern(Some(variable), labels, properties) => Seq(BoundNodeVar(variable, labels))
    case RelationshipChain(element, relationship, rightNode) => boundVariablesFromElement(element) ++ boundVariablesFromRelationship(relationship) ++ boundVariablesFromElement(rightNode)
  }

  def boundVariablesFromRelationship(relationship: RelationshipPattern) = {
    relationship.variable.map(v => BoundRelationVar(v, relationship.types)).toSeq
  }

  def boundVariablesFromPart(part: QueryPart) = part match {
    case SingleQuery(clauses) =>
      val boundVars = clauses collect {
        case Match(optional, Pattern(patternParts), hints, where) =>
          val elements = patternParts.map(_.element)
          elements.flatMap(boundVariablesFromElement)
      }
      boundVars.flatten
  }

  private val parser = new CypherParser

  def check(query: String): QueryResult = Try(parser.parse(query)) match {
    case Success(Query(None, part)) =>
      val boundVars = boundVariablesFromPart(part)
      val varMap = boundVars.groupBy(_.variable.name)
      val errors = mutable.ArrayBuffer.empty[String]

      val duplicateVars = varMap collect { case (name, _::_::_) => name }
      if (duplicateVars.nonEmpty) //TODO: handle duplicates as additional? matches
        errors += s"""found duplicate variables: ${duplicateVars.mkString(", ")}"""

      val undefinedVars = part.returnColumns.filterNot(varMap.contains)
      if (undefinedVars.nonEmpty)
        errors += s"""found undefined variables in return: ${undefinedVars.mkString(", ")}"""

      if (errors.isEmpty) {
        val returnedVars = part.returnColumns.map(r => varMap.apply(r).head)
        QuerySuccess(returnedVars)
      } else QueryFailure(errors.toList)
    case Success(res) => QueryFailure(List(s"unexpected parse result: $res"))
    case Failure(e) => QueryFailure(List(s"parse error: ${e.getMessage}"))
  }
}
