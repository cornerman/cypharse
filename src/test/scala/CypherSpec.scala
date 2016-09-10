import macroni.CompileSpec

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

  "cypher string interpolation" >> {
    q"""import cypharse._; cypher"match (n:FOO) return n"""" must compile.to(
      containTree(q""""match (n:FOO) return n"""")
    )
  }

  "match node" >> {
    val query = "match (n:FOO) return n"
    q"cypharse.Cypher($query)" must compile.to(cypherQuery(query))
  }

  "match node with params properties" >> {
    val query = "match (n:FOO {`id`: {params}.id}) return n"
    q"cypharse.Cypher($query)" must compile.to(cypherQuery(query))
  }

  "match relation" >> {
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

  "detect undefined variable" >> {
    val query = "match (n) return m"
    q"cypharse.Cypher($query)" must abort(
      "reflective typecheck has failed: ERROR: Variable `m` not defined (line 1, column 18 (offset: 17))"
    )
  }

  "detect syntax error" >> {
    val query = "catch (n) return n"
    q"cypharse.Cypher($query)" must abort(
      "reflective typecheck has failed: ERROR: parse error: Invalid input 't': expected 'l/L' (line 1, column 3 (offset: 2))"
    )
  }
}
