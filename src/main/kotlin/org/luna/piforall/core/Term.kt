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
    data class Lam(val name: Name, val body: Term) : Term()
    data class App(val t1: Term, val t2: Term) : Term()
    data class Pi(val binder: Name, val dom: Type, val codom: Type) : Term()
    object Univ : Term()
}


/**
 * Values
 */
sealed class Value {
    data class VVar(val lvl: Lvl) : Value()
    data class VLam(val name: Name, val body: Closure) : Value()
    data class VApp(val v1: Value, val v2: Lazy<Value>) : Value()
    data class VPi(val binder: Name, val dom: Lazy<VType>, val codom: Closure) : Value()
    object VUniv : Value()

    data class Closure(val env: Env, val body: Term) {
        fun applyTo(v: Lazy<Value>): Value = Normalizer(env.prepend(v.value)).eval(body)
    }
}