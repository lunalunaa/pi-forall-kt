package org.luna.piforall

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import org.luna.piforall.core.CTerm

object Parser {
    val termParser = object : Grammar<CTerm>() {

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
}