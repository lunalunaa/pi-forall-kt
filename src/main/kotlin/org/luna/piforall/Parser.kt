package org.luna.piforall

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.*
import com.github.h0tk3y.betterParse.parser.Parser
import org.luna.piforall.core.CDecl
import org.luna.piforall.core.CTerm
import org.luna.piforall.core.Program

object Parser {

    private val termParser = object : Grammar<CTerm>() {

        val ws by regexToken("\\s+", ignore = true)
        val lPar by literalToken("(")
        val rPar by literalToken(")")
        val slash by literalToken("\\")
        val point by literalToken(".")
        val colon by literalToken(":")
        val arrow by literalToken("->")
        val univ by literalToken("U")
        val varName by regexToken("\\w+")


        val universe by univ use { CTerm.CUniv }
        val variable by varName use { CTerm.CVar(text) }
        val lambda by -slash * varName * -point * parser { term } use { CTerm.CLam(t1.text, t2) }
        val pi by (-lPar * varName * -colon * parser { term } * -rPar) *
                -arrow * parser { term } use { CTerm.CPi(t1.text, t2, t3) }
        val termWithPar by -lPar * parser { term } * -rPar
        val nonApp by universe or termWithPar or lambda or variable or pi

        val term: Parser<CTerm> by leftAssociative(nonApp, optional(ws)) { a, _, b -> CTerm.CApp(a, b) }

        override val rootParser: Parser<CTerm> by term
    }

    // The caveat: Grammar will only collect by-delegated tokenizers declared IN THE SAME GRAMMAR CLASS.
    // This means the default tokenizer will fail if not all tokens are declared inside it
    // TODO: reimplement this by overriding the defaultParser
    // TODO: make parser able to parse "(A B: U)"
    private val programParser = object : Grammar<Program>() {

        val lineSeparator by regexToken("\\n+")
        val ws by regexToken("\\s+", ignore = true)
        val equalSign by literalToken("=")
        val colon by literalToken(":")


        val lPar by literalToken("(")
        val rPar by literalToken(")")
        val slash by literalToken("\\")
        val point by literalToken(".")
        val arrow by literalToken("->")
        val univ by literalToken("U")
        val varName by regexToken("\\w+")


        val universe by univ use { CTerm.CUniv }
        val variable by varName use { CTerm.CVar(text) }

        val atom by universe or variable or (-lPar * parser { term } * -rPar)
        val spine by leftAssociative(atom, optional(ws)) { a, _, b -> CTerm.CApp(a, b) }

        val lambda by -slash * varName * -point * parser { term } use { CTerm.CLam(t1.text, t2) }

        // (a b c d ... : t) and so on
        val doms by zeroOrMore(-lPar * zeroOrMore(varName) * -colon * parser { term } * -rPar)

        // TODO: rename this
        val pi2 by doms * -arrow * parser { term } map { (domBindings, codom) ->
            domBindings.foldRight(codom) { (binders, dom), acc ->
                binders.foldRight(acc) { name, ac -> CTerm.CPi(name.text, dom, ac) }
            }
        }

        //val pi by (-lPar * varName * -colon * parser { term } * -rPar) *
        // -arrow * parser { term } use { CTerm.CPi(t1.text, t2, t3) }

        val termWithPar by -lPar * parser { term } * -rPar
        val nonApp by universe or termWithPar or lambda or variable or pi2 use {
            //println(this)
            this
        }

        val term: Parser<CTerm> by leftAssociative(nonApp, optional(ws)) { a, _, b ->
            //println(CTerm.CApp(a, b))
            CTerm.CApp(a, b)
        }

        // (A: U) -> A -> A -> A

        //val funOrTerm by

        val decl by varName * -colon * term * -equalSign * term use {
            //println(CDecl(t1.text, t2, t3))
            CDecl(t1.text, t2, t3)
        }
        val decls by separatedTerms(decl, lineSeparator)


        override val rootParser: Parser<Program> by decls
    }

    private fun TokenMatch.reportLocation(): String = "\"$text\" at $offset ($row:$column)"

    private fun ErrorResult.reportErr(): String = when (this) {
        is MismatchedToken -> "Expected: \"${expected}\" but found ${found.reportLocation()}"
        is UnparsedRemainder -> "Unexpected token \"${startsWith.reportLocation()}\" at the end"
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