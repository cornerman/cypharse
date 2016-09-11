import scala.language.experimental.macros

package object cypharse {
  implicit class CypherHelper(val sc: StringContext) extends AnyVal {
    def cypher(args: Any*): String = macro CypherMacro.stringImpl
  }
}
