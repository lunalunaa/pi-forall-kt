package org.luna.piforall

import org.luna.piforall.core.CTerm
import org.luna.piforall.core.Normalizer
import org.luna.piforall.core.Program
import org.luna.piforall.core.TypeChecker
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

class CLI(debugMode: Boolean) {

    private val prompt = ">"
    private val typeChecker = TypeChecker(debugMode)

    private fun parseProgram(input: String): Program? = try {
        Parser.parseDecl(input)
    } catch (e: Exception) {
        println(e.message)
        null
    }

    private fun parseTerm(input: String): CTerm? = try {
        Parser.parseTerm(input)
    } catch (e: Exception) {
        println(e.message)
        null
    }

    private tailrec fun readInput(): String = readLine() ?: readInput()

    private tailrec fun readAndParse(): CTerm {
        val parsed = parseTerm(readInput())
        return if (parsed != null) parsed
        else {
            printPrompt()
            readAndParse()
        }
    }

    private fun printPrompt(): Unit = print(prompt)

    fun repl() {
        while (true) {
            printPrompt()
            val tyEvaluated = typeChecker.typeCheckAgainstUniv(readAndParse()) ?: continue

            printPrompt()
            val tm = readAndParse()
            val nf = typeChecker.checkAndNormalize(tm, tyEvaluated)
            if (nf != null) {
                println(nf)
            }
        }
    }


    fun typeCheckFile(fileName: String): Boolean {
        val allLinesJoined = try {
            Files.readAllLines(Paths.get(fileName)).joinToString(separator = "\n")
        } catch (e: FileNotFoundException) {
            println("$e not found")
            null
        }

        println(allLinesJoined)

        // TODO: this is ugly
        if (allLinesJoined != null) {
            val prog = parseProgram(allLinesJoined)
            if (prog != null) {
                val checkedDeclsPair = typeChecker.checkDecls(prog)
                if (checkedDeclsPair != null) {
                    val (checkedDecls, env) = checkedDeclsPair

                    // This don't work. I don't know exactly why???
                    //println(Normalizer(env).normalize(checkedDecls.last().def).prettyPrint(0, emptyList()))
                    print(Normalizer.quote(env.first()).pretty() + " : ")
                    println(Normalizer.quote(checkedDecls.last().sig).pretty())
                    return true
                }
            }
        }

        return false
    }
}

// TODO: write tests
fun main() {
    //CLI(false).repl()
    CLI(false).typeCheckFile("hello.txt")
}