package org.luna.piforall.core

import org.luna.piforall.util.prepend


typealias Type = Term
typealias Ix = Int
typealias Lvl = Int
typealias Name = String
typealias VType = Value
typealias Env = List<Value>

/**
Terms
 */
sealed class Term {
    data class Var(val idx: Ix) : Term()
    data class Lam(val binder: Name, val body: Term) : Term()
    data class App(val t1: Term, val t2: Term) : Term()
    data class Pi(val binder: Name, val dom: Type, val codom: Type) : Term()
    object Univ : Term() {
        override fun toString(): String = "U"
    }

    private fun pretty(lvl: Lvl, varList: List<Name>): String = when (this) {
        is App -> "(${t1.pretty(lvl, varList)} ${t2.pretty(lvl, varList)})"
        is Lam -> "λ $binder. ${body.pretty(lvl + 1, varList.prepend(binder))}"
        is Pi -> "($binder : ${dom.pretty(lvl, varList)}) -> ${codom.pretty(lvl + 1, varList.prepend(binder))}"
        is Univ -> "U"
        is Var -> varList[idx]
    }

    // this might throw this exception
    @Throws(IndexOutOfBoundsException::class)
    fun pretty(): String = pretty(0, emptyList())
}

/**
 * Values
 */
sealed class Value {
    data class VVar(val lvl: Lvl) : Value()
    data class VLam(val binder: Name, val body: Closure) : Value()
    data class VApp(val v1: Value, val v2: Lazy<Value>) : Value()
    data class VPi(val binder: Name, val dom: Lazy<VType>, val codom: Closure) : Value()
    object VUniv : Value() {
        override fun toString(): String = "VUniv"
    }

    // to substitute something (say, k-th free var) in the closure (environment), just substitute in the top-most env
    data class Closure(val env: Env, val body: Term) {
        fun applyTo(v: Lazy<Value>): Value = Normalizer(env.prepend(v.value)).eval(body)
    }
}

data class CheckedDecl(val name: Name, val sig: VType, val def: Term) {
    override fun toString(): String = "$name: $sig\n$name = $def"
}

