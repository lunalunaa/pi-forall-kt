package org.luna.piforall

import org.luna.piforall.core.*

object CLI {

    private val elaborator = Elaborator()
    private const val prompt = ">"

    private fun parse(input: String): CTerm? = TODO()
    private fun typeCheck(tm: CTerm, ty: CType): Term? =
        try {
            val tyElaborated = elaborator.checkTy(ty, Value.VUniv)
            elaborator.checkTy(tm, Normalizer.eval(tyElaborated))
        } catch (e: TypeCheckError) {
            e.report()
            null
        }

    private fun checkAndNormalize(tm: CTerm, ct: CType): Term? {
        val tmElaborated = typeCheck(tm, ct)
        return if (tmElaborated != null) {
            Normalizer.normalize(tmElaborated)
        } else {
            null
        }
    }

    private tailrec fun readInput(): String = readLine() ?: readInput()
    private tailrec fun readAndParse(): CTerm = parse(readInput()) ?: readAndParse()
    private fun printPrompt(): Unit = print(prompt)

    fun REPL() {
        while (true) {
            printPrompt()
            val tm = readAndParse()
            val ty = readAndParse()
            val nf = checkAndNormalize(tm, ty)
            if (nf != null) {
                println(nf)
            }
        }
    }
}

fun main() {
    CLI.REPL()
}