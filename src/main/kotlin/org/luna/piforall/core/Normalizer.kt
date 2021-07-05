package org.luna.piforall.core


data class Normalizer(val env: Env) {
    fun eval(tm: Term): Value = when (tm) {
        is Term.Var -> env[tm.idx]
        is Term.App -> {
            val v1 = eval(tm.t1)
            val v2 = eval(tm.t2)
            when (v1) {
                is Value.VLam -> v1.body.applyTo { v2 }
                else -> Value.VApp(v1) { v2 }
            }
        }
        is Term.Lam -> Value.VLam(tm.name, Closure(env, tm.body))
        is Term.Pi -> Value.VPi(tm.binder, { eval(tm.dom) }, Closure(env, tm.codom))
        is Term.Univ -> Value.VUniv
    }

    private fun lvl2Ix(varLvl: Lvl, totalLvl: Lvl): Ix = totalLvl - varLvl - 1

    private fun quote(lvl: Lvl, v: Value): Term = when (v) {
        is Value.VVar -> Term.Var(lvl2Ix(lvl, v.lvl))
        is Value.VApp -> Term.App(quote(lvl, v.v1), quote(lvl, v.v2()))
        is Value.VLam -> Term.Lam(v.name, quote(lvl + 1, v.body.applyTo { Value.VVar(lvl) }))
        is Value.VPi -> Term.Pi(v.binder, quote(lvl, v.dom()), quote(lvl + 1, v.codom.applyTo { Value.VVar(lvl) }))
        is Value.VUniv -> Term.Univ
    }

    fun normalize(tm: Term): Term = quote(env.size, eval(tm))
}