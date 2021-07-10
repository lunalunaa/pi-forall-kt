package org.luna.piforall

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.luna.piforall.core.*

class CLI(debugMode: Boolean) {

    private val elaborator = Elaborator(debugMode)
    private val prompt = ">"

    private fun parse(input: String): CTerm? = TODO()
    private fun typeCheck(tm: CTerm, ty: CType): Term? =
        try {
            elaborator.checkTy(tm, tyEvaluated)
        } catch (e: TypeCheckError) {
            println(e.report())
            null
        }

    private fun checkAndNormalize(tm: CTerm, ct: VType): Term? {
        val tmElaborated = typeCheck(tm, ct)
        return if (tmElaborated != null) {
            Normalizer.normalize(tmElaborated)
        } else {
            null
        }
    }

    private tailrec fun readInput(): String = readLine() ?: readInput()
    private tailrec fun readAndParse(): CTerm {
        val parsed = parse(readInput())
        return if (parsed != null) parsed
        else {
            printPrompt()
            readAndParse()
        }
    }

    private fun printPrompt(): Unit = print(prompt)

    private fun typeCheckAgainstUniv(ty: CTerm): VType? = try {
        val tyElaborated = elaborator.checkTy(ty, Value.VUniv)
        Normalizer.eval(tyElaborated)
    } catch (e: TypeCheckError) {
        println(e.report())
        null
    }

    fun repl() {
        while (true) {
            printPrompt()
            val tyEvaluated = typeCheckAgainstUniv(readAndParse()) ?: continue

            printPrompt()
            val tm = readAndParse()
            val nf = checkAndNormalize(tm, tyEvaluated)
            if (nf != null) {
                println(nf)
            }
        }
    }
}

fun main() {
    CLI(false).repl()
}