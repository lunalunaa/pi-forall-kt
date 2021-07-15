package org.luna.piforall.core


data class Normalizer(val env: Env) {

    constructor() : this(emptyList())

    fun eval(tm: Term): Value = when (tm) {
        is Term.Var -> env[tm.idx]  // indices -- the top of the environment stack, if use levels, then env[env.size - tm.idx]
        is Term.App -> {
            val t2Lazy = lazy { eval(tm.t2) }
            when (val v1 = eval(tm.t1)) {
                is Value.VLam -> v1.body.applyTo(t2Lazy)
                else -> Value.VApp(v1, t2Lazy)
            }
        }
        is Term.Lam -> Value.VLam(tm.binder, Value.Closure(env, tm.body))
        is Term.Pi -> Value.VPi(tm.binder, lazy { eval(tm.dom) }, Value.Closure(env, tm.codom))
        is Term.Univ -> Value.VUniv
    }

    private fun lvl2Ix(totalLvl: Lvl, varLvl: Lvl): Ix = totalLvl - varLvl - 1

    private fun quote(lvl: Lvl, v: Value): Term = when (v) {
        is Value.VVar -> Term.Var(lvl2Ix(lvl, v.lvl))   // levels -- convert it back
        is Value.VApp -> Term.App(quote(lvl, v.v1), quote(lvl, v.v2.value))
        is Value.VLam -> Term.Lam(v.binder, quote(lvl + 1, v.body.applyTo(lazy { Value.VVar(lvl) })))
        is Value.VPi -> Term.Pi(
            v.binder,
            quote(lvl, v.dom.value),
            quote(lvl + 1, v.codom.applyTo(lazy { Value.VVar(lvl) }))
        )
        is Value.VUniv -> Term.Univ
    }

    fun normalize(tm: Term): Term = quote(env.size, eval(tm))

    companion object {
        fun normalize(tm: Term): Term = Normalizer().normalize(tm)
        fun eval(tm: Term): Value = Normalizer().eval(tm)
        fun quote(v: Value) = Normalizer().quote(0, v)
    }
}