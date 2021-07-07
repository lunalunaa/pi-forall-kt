package org.luna.piforall

import org.luna.piforall.core.*
import org.luna.piforall.core.CTerm.*

object CLI {

    private val elaborator = Elaborator()
    private const val prompt = ">"

    private fun parse(input: String): CTerm? = TODO()
    private fun typeCheck(tm: CTerm, ty: CType): Term? =
        try {
            val tyElaborated = elaborator.checkTy(ty, Value.VUniv)
            elaborator.checkTy(tm, Normalizer.eval(tyElaborated))
        } catch (e: TypeCheckError) {
            println(e.report())
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

    fun debug(ct: CTerm, tm: CType) {
        println("DEBUGGING")
        val tyckResult = typeCheck(ct, tm)
        if (tyckResult != null) {
            println("SUCCESS! Now normalizing")
            println(Normalizer.normalize(tyckResult))
        }
    }

    fun test1() {
        val idType = CTerm.CPi("A", CTerm.CUniv, CTerm.CPi("_", CTerm.CVar("A"), CTerm.CVar("A")))
        val idLam = CTerm.CLam("A", CTerm.CLam("x", CTerm.CVar("x")))
        val constType = CTerm.CPi(
            "A", CTerm.CUniv,
            CTerm.CPi(
                "B", CTerm.CUniv,
                CTerm.CPi(
                    "_1", CTerm.CVar("A"),
                    CTerm.CPi("_2", CTerm.CVar("B"), CTerm.CVar("A"))
                )
            )
        )


        val constLam = CLam("A", CLam("B", CLam("x", CLam("y", CVar("x")))))
        val idAppConst = CApp(idLam, constLam)


        val notidType = CTerm.CPi("A", CTerm.CUniv, CTerm.CPi("_", CTerm.CVar("A"), CUniv))
        val notidLam = CTerm.CLam("A", CTerm.CLam("x", CTerm.CVar("A")))

        //println("this is not id")
        debug(notidLam, notidType)
        println(Normalizer.eval(Term.App(Term.Lam("A", Term.Lam("x", Term.Var(1))), Term.Univ)))

        //println("this is id")
        //println(typeCheck(idLam, idType))
        //println(Normalizer.eval(typeCheck(idLam, idType)!!))

        //println("this is const")
        //println(typeCheck(constLam, constType))
        //println(Normalizer.normalize(typeCheck(constLam, constType)!!))
    }
}

fun main() {
    //CLI.REPL()

    CLI.test1()
}