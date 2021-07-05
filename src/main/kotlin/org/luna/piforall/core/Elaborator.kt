package org.luna.piforall.core

import org.luna.piforall.util.prepend
import org.luna.piforall.core.TypeCheckError.*

typealias Types = List<Pair<Name, VType>>

data class Context(
    val env: Env,
    val types: Types,
    val lvl: Lvl
) {

    fun bind(name: Name, ty: () -> VType): Context {
        return Context(env.prepend(Value.VVar(lvl)), types + (name to ty()), lvl + 1)
    }

    fun define(name: Name, v: () -> Value, ty: () -> VType): Context {
        return Context(env.prepend(v()), types + (name to ty()), (lvl + 1))
    }

    companion object Factory {
        fun emptyCxt(): Context {
            return Context(emptyList(), emptyList(), 0)
        }
    }
}

// TODO: use mutable map?

class Elaborator {

    @Throws(TypeCheckError::class)
    fun checkTy(ctx: Context, ct: CTerm, expected: VType): Term {
        if (ct is CTerm.CLam && expected is Value.VPi) {
            val tm = checkTy(ctx.bind(ct.name, expected.dom), ct.body, expected.codom.applyTo { Value.VVar(ctx.lvl) })
            return Term.Lam(ct.name, tm)
        } else {
            val (tm, inferred) = inferTy(ctx, ct)
            return if (checkConv(ctx.lvl, expected, inferred)) tm else throw TypeMismatch(expected, inferred)
        }
    }

    // TODO: Double check non-strict semantics
    @Throws(TypeCheckError::class)
    fun inferTy(ctx: Context, ct: CTerm): Pair<Term, VType> = when (ct) {
        is CTerm.CVar -> ctx.types.run {
            val idx = indexOfFirst { (name, _) -> name == ct.name }
            return if (idx != -1) Pair(Term.Var(idx), get(idx).second) else throw VarOutScope(ct)
        }
        is CTerm.CApp -> {
            val (tm1, inferred) = inferTy(ctx, ct.tm1)
            if (inferred is Value.VPi) {
                val tm2 = checkTy(ctx, ct.tm2, inferred.dom())
                Pair(Term.App(tm1, tm2), inferred.codom.applyTo { Normalizer(ctx.env).eval(tm2) })
            } else {
                throw ExpectedFunType(inferred)
            }
        }
        is CTerm.CLam -> throw CannotInferLambda
        is CTerm.CPi -> {
            val dom = checkTy(ctx, ct.dom, Value.VUniv)
            val codom = checkTy(ctx.bind(ct.binder) { Normalizer(ctx.env).eval(dom) }, ct.codom, Value.VUniv)
            Pair(Term.Pi(ct.binder, dom, codom), Value.VUniv)
        }
        is CTerm.CUniv -> Pair(Term.Univ, Value.VUniv)
    }
}