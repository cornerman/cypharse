package cypharse

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import scala.annotation.compileTimeOnly

//TODO: query params
case class CypherQuery(query: String)

class CypherTranslator[C <: Context](val context: C) {
  import context.universe._

  implicit val lift = Liftable[CypherQuery] { q =>
    q"_root_.cypharse.CypherQuery(${q.query})"
  }

  def translate(tree: Tree): Tree = tree match {
    case Literal(Constant(query: String)) => CypherChecker.check(query) match {
      case Right(returnedVars) =>
        // do something...
        val cypher = CypherQuery(query)
        q"$cypher"
      case Left(errors) => context.abort(context.enclosingPosition, errors.map(s"ERROR: " + _).mkString("\n"))
    }
    case _ => context.abort(context.enclosingPosition, s"Expected literal string constant, but found ${tree}")
  }
}

object CypherTranslator {
  def apply(c: Context): CypherTranslator[c.type] = new CypherTranslator(c)
}

object CypherMacro {
  def impl(c: Context)(arg: c.Expr[String]): c.Expr[CypherQuery] = {
    val translator = CypherTranslator(c)
    val tree = translator.translate(arg.tree)
    c.Expr[CypherQuery](tree)
  }
}

@compileTimeOnly("only for compile time expansion")
object Cypher {
  def apply(arg: String): CypherQuery = macro CypherMacro.impl
}
