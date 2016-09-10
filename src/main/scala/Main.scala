package cypharse

import org.neo4j.cypher.internal.frontend.v3_0.parser.CypherParser
import org.neo4j.cypher.internal.frontend.v3_0.ast._

object Main extends App {
  def printReturns(query: String) = {
    println(s"QUERY: $query")
    CypherChecker.check(query) match {
      case QuerySuccess(returnedVars) =>
        val returnedDefs = returnedVars collect {
          case BoundNodeVar(v, labels) => s"Node (${v.name}): ${labels.map(_.name).mkString}"
          case BoundRelationVar(v, relTypes) => s"Relation [${v.name}]: ${relTypes.map(_.name).mkString}"
        }

        println(s"""RETURNS:\n ${returnedDefs.mkString("\n ")}""")
      case QueryFailure(errors) => println(errors.map(s"ERROR: " + _).mkString("\n"))
    }
  }

  printReturns("match (n:FOO) return n")

  printReturns("match (n:SRC)-[r:REL]->(m:SINK) return n,r,m")

  printReturns("match (n:FOO) return m")
  printReturns("match (n:FOO) peter return m")
}
