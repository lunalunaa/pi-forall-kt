package org.luna.piforall.parse

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.parser.Parser
import org.luna.piforall.core.CDecl
import org.luna.piforall.core.CTerm
import org.luna.piforall.core.Program

internal object TOKENS : Grammar<CTerm>() {
  val LINE_SEPARATOR by regexToken("\\n+")
  val WHITESPACE by regexToken("\\s+", ignore = true)
  val UNDERSCORE by literalToken("_")
  val EQUAL_SIGN by literalToken("=")
  val COLON by literalToken(":")
  val LEFT_BRACKET by literalToken("(")
  val RIGHT_BRACKET by literalToken(")")
  val SLASH by literalToken("\\")
  val POINT by literalToken(".")
  val ARROW by literalToken("->")
  val UNIVERSE by literalToken("U")
  val IDENTIFIER by regexToken("\\w+")

  override val rootParser: Parser<CTerm>
    get() = TODO("DO NOT USE THIS")
}


object Parser {

  private val termParser = object : Grammar<CTerm>() {
    override val tokens: List<Token>
      get() = TOKENS.tokens


    val universeP by TOKENS.UNIVERSE asJust (CTerm.CUniv)
    val variableP by TOKENS.IDENTIFIER use { CTerm.CVar(text) }
    val binderP by TOKENS.IDENTIFIER or TOKENS.UNDERSCORE

    val atomP by universeP or variableP or (-TOKENS.LEFT_BRACKET * parser { termP } * -TOKENS.RIGHT_BRACKET)
    val spineP by leftAssociative(atomP, optional(TOKENS.WHITESPACE)) { a, _, b -> CTerm.CApp(a, b) }
    val lambdaP by -TOKENS.SLASH * oneOrMore(binderP) * -TOKENS.POINT * parser { termP } map { (binders, body) ->
      binders.foldRight(body) { binder, acc ->
        CTerm.CLam(binder.text, acc)
      }
    }

    // (a b c d ... : t) (e f g h ... : u) and so on
    val domsP by zeroOrMore(-TOKENS.LEFT_BRACKET * oneOrMore(binderP) * -TOKENS.COLON * parser { termP } * -TOKENS.RIGHT_BRACKET)

    val piP by domsP * -TOKENS.ARROW * parser { termP } map { (domBindings, codom) ->
      domBindings.foldRight(codom) { (binders, dom), acc ->
        binders.foldRight(acc) { name, ac -> CTerm.CPi(name.text, dom, ac) }
      }
    }

    val funOrSpineP by spineP * optional(TOKENS.ARROW) bind { (sp, arr) ->
      if (arr == null) pure(sp) else termP map { CTerm.CPi(binder = "_", sp, it) }
    }

    val termP: Parser<CTerm> by lambdaP or piP or funOrSpineP

    override val rootParser: Parser<CTerm> by termP
  }

  // The caveat: Grammar will only collect by-delegated tokenizers declared IN THE SAME GRAMMAR CLASS.
  // This means the default tokenizer will fail if not all tokens are declared inside it
  private val programParser = object : Grammar<Program>() {
    override val tokens: List<Token>
      get() = TOKENS.tokens


    val declP by TOKENS.IDENTIFIER * -TOKENS.COLON * termParser * -TOKENS.EQUAL_SIGN * termParser use {
      CDecl(t1.text, t2, t3)
    }

    // removes trailing line separators
    val declsP by -optional(TOKENS.LINE_SEPARATOR) * separatedTerms(
      declP,
      TOKENS.LINE_SEPARATOR,
    ) * -optional(TOKENS.LINE_SEPARATOR)


    override val rootParser: Parser<Program> by declsP
  }


  private fun TokenMatch.reportLocation(): String = "\"$text\" at $offset ($row:$column)"

  private fun ErrorResult.reportErr(): String = when (this) {
    is MismatchedToken -> "Expected: \"${expected}\" but found ${found.reportLocation()}"
    is UnparsedRemainder -> "Unexpected token ${startsWith.reportLocation()}"
    is NoMatchingToken -> "Found ${tokenMismatch.reportLocation()} but no matching token"
    is UnexpectedEof -> "Expecting $expected at the end"
    else -> ""
  }

  private fun processParserErrs(err: ErrorResult) {
    when (err) {
      // only show the first parser error
      is AlternativesFailure -> println(err.errors.first().reportErr())
      else -> println(err.reportErr())
    }
  }

  private fun <T> tryParse(str: String, parser: Grammar<T>): T? = try {
    parser.tryParseToEnd(str).toParsedOrThrow().value
  } catch (err: ParseException) {
    processParserErrs(err.errorResult)
    null
  }

  fun parseTerm(str: String): CTerm? = tryParse(str, termParser)

  /**
   * Parse a declaration, the first param is like decl: Type, the second is like decl =
   */
  fun parseDecl(str: String): List<CDecl>? = tryParse(str, programParser)
}
