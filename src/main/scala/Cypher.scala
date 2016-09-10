package cypharse

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import scala.annotation.compileTimeOnly

//TODO: query params
case class CypherQuery(query: String)

class CypherTranslator[C <: Context](val context: C) {
  import context.universe._

  def translate(tree: Tree): String = tree match {
    case Literal(Constant(query: String)) => CypherChecker.check(query) match {
      case Right(_) => query
      case Left(errors) => context.abort(context.enclosingPosition, errors.map(s"ERROR: " + _).mkString("\n"))
    }
    case _ => context.abort(context.enclosingPosition, s"Expected literal string constant, but found ${tree}")
  }

  def translateFromContext = context.prefix.tree match {
    case Apply(_, List(Apply(_, part :: Nil))) => translate(part)
    case _ => context.abort(context.enclosingPosition, s"Expected one string")
  }
}

object CypherTranslator {
  def apply(c: Context): CypherTranslator[c.type] = new CypherTranslator(c)
}

object CypherMacro {

  def macroImpl(c: Context)(arg: c.Expr[String]): c.Expr[CypherQuery] = {
    import c.universe._
    val translator = CypherTranslator(c)
    val query = translator.translate(arg.tree)
    c.Expr[CypherQuery](q"_root_.cypharse.CypherQuery($query)")
  }

  def stringImpl(c: Context)(args: c.Expr[Any]*): c.Expr[String] = {
    import c.universe._
    val translator = CypherTranslator(c)
    val query = translator.translateFromContext
    c.Expr[String](q"$query")
  }
}

@compileTimeOnly("only for compile time expansion")
object Cypher {
  def apply(arg: String): CypherQuery = macro CypherMacro.macroImpl
}
