package org.luna.piforall.core

import org.luna.piforall.core.TypeCheckError.*
import org.luna.piforall.util.prepend

typealias Types = List<Pair<Name, VType>>

// TODO: use mutable map?

data class Context(
    val env: Env,
    val types: Types,
    val lvl: Lvl
) {

    fun bind(name: Name, ty: Lazy<VType>): Context {
        return Context(env.prepend(Value.VVar(lvl)), types.prepend(name to ty.value), lvl + 1)
    }

    fun define(name: Name, v: Lazy<Value>, ty: Lazy<VType>): Context {
        return Context(env.prepend(v.value), types.prepend(name to ty.value), (lvl + 1))
    }

    companion object Factory {
        fun emptyCxt(): Context {
            return Context(emptyList(), emptyList(), 0)
        }
    }
}


class Elaborator {

    @Throws(TypeCheckError::class)
    fun checkTy(ctx: Context, ct: CTerm, expected: VType): Term {
        return if (ct is CTerm.CLam && expected is Value.VPi) {
            val tm =
                checkTy(
                    ctx.bind(ct.binder, expected.dom),
                    ct.body,
                    expected.codom.applyTo(lazy { Value.VVar(ctx.lvl) })
                )
            Term.Lam(ct.binder, tm)
        } else {
            val (tm, inferred) = inferTy(ctx, ct)
            if (checkConv(ctx.lvl, expected, inferred)) tm else throw TypeMismatch(expected, inferred)
        }
    }

    @Throws(TypeCheckError::class)
    fun inferTy(ctx: Context, ct: CTerm): Pair<Term, VType> = when (ct) {
        is CTerm.CVar -> ctx.types.run {
            val idx = indexOfFirst { (name, _) -> name == ct.name }
            return if (idx != -1) Pair(Term.Var(idx), get(idx).second) else throw VarOutScope(ct)
        }
        is CTerm.CApp -> {
            val (tm1, inferred) = inferTy(ctx, ct.tm1)
            if (inferred is Value.VPi) {
                val tm2 = checkTy(ctx, ct.tm2, inferred.dom.value)
                Pair(Term.App(tm1, tm2), inferred.codom.applyTo(lazy { Normalizer(ctx.env).eval(tm2) }))
            } else {
                throw ExpectedFunType(inferred)
            }
        }
        is CTerm.CLam -> throw CannotInferLambda
        is CTerm.CPi -> {
            val dom = checkTy(ctx, ct.dom, Value.VUniv)
            val codom = checkTy(ctx.bind(ct.binder, lazy { Normalizer(ctx.env).eval(dom) }), ct.codom, Value.VUniv)
            Pair(Term.Pi(ct.binder, dom, codom), Value.VUniv)
        }
        is CTerm.CUniv -> Pair(Term.Univ, Value.VUniv)
    }

    /**
     * Check type without context
     */

    @Throws(TypeCheckError::class)
    fun checkTy(ct: CTerm, expected: VType): Term = checkTy(Context.emptyCxt(), ct, expected)

    /**
     * Infer type without context
     */
    @Throws(TypeCheckError::class)
    fun inferTy(ct: CTerm): Pair<Term, VType> = inferTy(Context.emptyCxt(), ct)
}

// TODO: make a DSL builder?
fun main() {
    val ct = CTerm.CPi("A", CTerm.CUniv, CTerm.CPi("_", CTerm.CVar("A"), CTerm.CVar("A")))
    val tm = CTerm.CLam("A", CTerm.CLam("x", CTerm.CVar("x")))
    val ty = Elaborator().checkTy(Context.emptyCxt(), ct, Value.VUniv)

    try {
        Normalizer.normalize(Elaborator().checkTy(tm, Normalizer.eval(ty)))
    } catch (e: TypeCheckError) {
        e.report()
    }
}