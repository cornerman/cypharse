import org.neo4j.cypher.internal.frontend.v3_0.parser.CypherParser
import org.neo4j.cypher.internal.frontend.v3_0.ast._

sealed trait BoundVariable {
  val variable: Variable
}

case class BoundRelationVar(variable: Variable, labels: Seq[RelTypeName]) extends BoundVariable
case class BoundNodeVar(variable: Variable, labels: Seq[LabelName]) extends BoundVariable

object Main extends App {
  def boundVariablesFromElement(element: PatternElement): Seq[BoundVariable] = element match {
    case NodePattern(Some(variable), labels, properties) => Seq(BoundNodeVar(variable, labels))
    case RelationshipChain(element, relationship, rightNode) => boundVariablesFromElement(element) ++ boundVariablesFromRelationship(relationship) ++ boundVariablesFromElement(rightNode)
  }

  def boundVariablesFromRelationship(relationship: RelationshipPattern) = {
    relationship.variable.map(v => BoundRelationVar(v, relationship.types)).toSeq
  }

  def boundVariablesFromPart(part: QueryPart) = part match {
    case SingleQuery(clauses) => clauses collect {
      case Match(optional, Pattern(patternParts), hints, where) =>
        val elements = patternParts.map(_.element)
        elements.flatMap(boundVariablesFromElement)
    }
  }

  val parser = new CypherParser

  def check(query: String) = parser.parse(query) match {
    case Query(None, part) =>
      println(s"Query: $query")
      val boundVars = boundVariablesFromPart(part).flatten
      println(boundVars)

      val returns = part.returnColumns map { returned =>
        boundVars.find(v => v.variable.name == returned) match {
          case Some(BoundNodeVar(v, labels)) => s"Node (${v.name}): ${labels.map(_.name).mkString}"
          case Some(BoundRelationVar(v, relTypes)) => s"Relation [${v.name}]: ${relTypes.map(_.name).mkString}"
          case None => s"Variable $returned is undefined"
        }
      }

      println(s"""returns:\n ${returns.mkString("\n ")}""")
  }


  check("match (n:FOO) return n")

  check("match (n:SRC)-[r:REL]->(m:SINK) return n,r,m")
}
