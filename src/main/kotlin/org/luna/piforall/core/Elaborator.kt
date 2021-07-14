package org.luna.piforall.core

import org.luna.piforall.core.TypeCheckError.*
import org.luna.piforall.util.Debugger.debugChecker
import org.luna.piforall.util.Debugger.debugInferer
import org.luna.piforall.util.prepend

typealias Types = List<Pair<Name, VType>>

data class Context(
    val env: Env,
    val types: Types,
    val lvl: Lvl
) {

    fun bind(name: Name, ty: Lazy<VType>): Context {
        /**
        Here it creates a VVar with CURRENT level, and pushes it into the stack.
        But can closures store values with indices?? That's a bad idea, because in de Bruijn index, all free variables
        have to shift when we go across another binder, this means we have to shift the entire closure.
         */
        return Context(env.prepend(Value.VVar(lvl)), types.prepend(name to ty.value), lvl + 1)
    }

    fun define(name: Name, v: Lazy<Value>, ty: Lazy<VType>): Context {
        return Context(env.prepend(v.value), types.prepend(name to ty.value), (lvl + 1))
    }

    companion object {
        fun emptyContext(): Context {
            return Context(emptyList(), emptyList(), 0)
        }
    }
}

// TODO: refactor code so that context is a member of the elaborator

class Elaborator(
    private val context: Context = Context.emptyContext(),
    private val debugMode: Boolean = false
) {

    @Throws(TypeCheckError::class)
    fun checkTy(ct: CTerm, expected: VType): Term {
        if (debugMode) debugChecker(context, ct, expected)

        return if (ct is CTerm.CLam && expected is Value.VPi) {
            val extendedElab = Elaborator(context.bind(ct.binder, expected.dom), debugMode)
            val tm = extendedElab.checkTy(ct.body, expected.codom.applyTo(lazy { Value.VVar(context.lvl) }))
            Term.Lam(ct.binder, tm)
        } else {
            val (tm, inferred) = inferTy(ct)
            if (checkConv(context.lvl, expected, inferred)) tm else throw TypeMismatch(expected, inferred, ct)
        }
    }

    @Throws(TypeCheckError::class)
    fun inferTy(ct: CTerm): Pair<Term, VType> {

        if (debugMode) debugInferer(context, ct)

        return when (ct) {

            is CTerm.CVar -> context.types.run {
                val idx = indexOfFirst { (name, _) -> name == ct.name }
                return if (idx != -1) Pair(Term.Var(idx), get(idx).second) else throw VarOutOfScope(ct)
            }
            is CTerm.CApp -> {
                val (tm1, inferred) = inferTy(ct.tm1)
                if (inferred is Value.VPi) {
                    val tm2 = checkTy(ct.tm2, inferred.dom.value)
                    Pair(Term.App(tm1, tm2), inferred.codom.applyTo(lazy { Normalizer(context.env).eval(tm2) }))
                } else {
                    throw ExpectedFunType(inferred)
                }
            }
            is CTerm.CLam -> throw CannotInferLambda

            // I think Pi can be either synthesized or checked
            is CTerm.CPi -> {
                val dom = checkTy(ct.dom, Value.VUniv)

                // defines a new elaborator which binds the binder with the codomain type
                val extendedElab =
                    Elaborator(context.bind(ct.binder, lazy { Normalizer(context.env).eval(dom) }), debugMode)
                val codom = extendedElab.checkTy(ct.codom, Value.VUniv)
                Pair(Term.Pi(ct.binder, dom, codom), Value.VUniv)
            }
            is CTerm.CUniv -> Pair(Term.Univ, Value.VUniv)
        }
    }
}

fun main() {

}