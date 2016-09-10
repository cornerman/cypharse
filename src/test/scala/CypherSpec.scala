import macroni.CompileSpec

import cypharse._

class CypherSpec extends CompileSpec {
  import scala.reflect.runtime.universe._

  "only takes literal constant string" >> {
    q"""val x = "match (n) return n"; cypharse.Cypher(x)""" must abort(
      "reflective typecheck has failed: Expected literal string constant, but found x"
    )
  }

  "parse node pattern" >> {
    q"""cypharse.Cypher("match (n:FOO) return n")""" must compile.to(
      //TODO: why :type?
      q"""(cypharse.CypherQuery.apply("match (n:FOO) return n")): cypharse.CypherQuery"""
    )
  }

  "parse relation pattern" >> {
    q"""cypharse.Cypher("match (n:N)-[r:R]->(m:M) return n,r,m")""" must compile.to(
      //TODO: why :type?
      q"""(cypharse.CypherQuery.apply("match (n:N)-[r:R]->(m:M) return n,r,m")): cypharse.CypherQuery"""
    )
  }
}
