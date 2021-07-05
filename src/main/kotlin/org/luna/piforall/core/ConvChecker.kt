package org.luna.piforall.core

fun checkConv(lvl: Lvl, v1: Value, v2: Value): Boolean {
    if (v1 is Value.VUniv) {
        return v2 is Value.VUniv
    } else if (v1 is Value.VPi && v2 is Value.VPi) {
        return checkConv(lvl, v1.dom(), v2.dom())
                && checkConv(lvl + 1, v1.codom.applyTo { Value.VVar(lvl) }, v2.codom.applyTo { Value.VVar(lvl) })
    } else if (v1 is Value.VLam && v2 is Value.VLam) {
        return checkConv(lvl, v1.body.applyTo { Value.VVar(lvl) }, v2.body.applyTo { Value.VVar(lvl) })
    } else if (v1 is Value.VLam) {
        return checkConv(lvl + 1, v1.body.applyTo { Value.VVar(lvl) }, Value.VApp(v2) { Value.VVar(lvl) })
    } else if (v2 is Value.VLam) {
        return checkConv(lvl + 1, Value.VApp(v1) { Value.VVar(lvl) }, v2.body.applyTo { Value.VVar(lvl) })
    } else if (v1 is Value.VVar) {
        return v2 is Value.VVar && v1.lvl == v2.lvl
    } else if (v1 is Value.VApp) {
        return v2 is Value.VApp && checkConv(lvl, v1.v1, v2.v1)
                && checkConv(lvl + 1, v1.v2(), v2.v2())
    } else {
        return false
    }
}