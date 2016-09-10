import macroni.CompileSpec

import cypharse._

class CypherSpec extends CompileSpec {
  import scala.reflect.runtime.universe._

  def cypherQuery(query: String): Tree = {
    //TODO: why :type?
    q"(cypharse.CypherQuery.apply($query)): cypharse.CypherQuery"
  }

  "only takes literal constant string" >> {
    q"""val x = "match (n) return n"; cypharse.Cypher(x)""" must abort(
      "reflective typecheck has failed: Expected literal string constant, but found x"
    )
  }

  "parse node pattern" >> {
    val query = "match (n:FOO) return n"
    q"cypharse.Cypher($query)" must compile.to(cypherQuery(query))
  }

  "parse relation pattern" >> {
    val query = "match (n:N)-[r:R]->(m:M) return n,r,m"
    q"cypharse.Cypher($query)" must compile.to(cypherQuery(query))
  }

  "create node" >> {
    val query = "create (n:FOO) return n"
    q"cypharse.Cypher($query)" must compile.to(cypherQuery(query))
  }

  "create unique node" >> {
    val query = "create unique (n:FOO) return n"
    q"cypharse.Cypher($query)" must compile.to(cypherQuery(query))
  }

  "merge node" >> {
    val query = "merge (n:FOO) return n"
    q"cypharse.Cypher($query)" must compile.to(cypherQuery(query))
  }
}
