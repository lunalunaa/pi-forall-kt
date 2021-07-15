package org.luna.piforall

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
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

        override val rootParser: Parser<CTerm> = term
    }

    // The caveat: Grammar will only collect by-delegated tokenizers declared IN THE SAME GRAMMAR CLASS.
    // This means the default tokenizer will fail if not all tokens are declared inside it
    // TODO: reimplement this by overriding the defaultParser
    private val programParser = object : Grammar<Program>() {

        val lineSeparator by regexToken("\\n+")
        val ws by regexToken("\\s+", ignore = true)
        val varName by regexToken("\\w+")
        val equalSign by literalToken("=")


        val decl by varName * termParser * -equalSign * termParser use { CDecl(t1.text, t2, t3) }
        val decls by oneOrMore(decl)

        override val rootParser: Parser<Program> = decls
    }

    /**
     * Parse a declaration, the first param is like decl: Type, the second is like decl =
     */
    fun parseDecl(str: String): List<CDecl> = programParser.parseToEnd(str)

    // TODO: add error handling, use tryParseToEnd
    fun parseTerm(str: String): CTerm = termParser.parseToEnd(str)
}