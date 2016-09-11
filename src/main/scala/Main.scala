package cypharse

import org.neo4j.cypher.internal.frontend.v3_0.parser.CypherParser
import org.neo4j.cypher.internal.frontend.v3_0.ast._

object Main extends App {
  def printErrors(errors: Seq[String]) = println(errors.map(s"ERROR: " + _).mkString("\n"))

  def printReturns(returnedVars: Seq[BoundVariable]) = {
    val returnedDefs = returnedVars collect {
      case BoundNodeVar(v, labels, properties) => s"Node (${v.name}): ${labels.map(_.name).mkString} {${properties}}"
      case BoundRelationVar(v, relTypes, properties) => s"Relation [${v.name}]: ${relTypes.map(_.name).mkString} {${properties}}"
    }
    println(s"""RETURNS:\n ${returnedDefs.mkString("\n ")}""")
  }

  def printQuery(query: String) = {
    import CypherChecker._
    CypherChecker.check(query)
      .fold(printErrors, s => CypherChecker.returns(s).fold(printErrors, printReturns))
  }

  printQuery("match (n:FOO) return n")
  printQuery("match (n:SRC)-[r:REL]->(m:SINK) return n,r,m")
  printQuery("match (n:FOO) return m")
  printQuery("match (n:FOO) peter return m")
  printQuery("match (n:FOO {bla: {stuff}}) return n")
  printQuery("match (n:FOO {bla: {stuff}.blubb.foo}) return n")
  printQuery("create (n:FOO {stuff}) return n")
}
