package org.luna.piforall.core

/**
 * Conversion checking
 */
fun checkConv(lvl: Lvl, v1: Value, v2: Value): Boolean {

  val currLvl = lazy { Value.VVar(lvl) }

  return when {
    v1 is Value.VUniv -> v2 is Value.VUniv
    v1 is Value.VPi && v2 is Value.VPi -> checkConv(lvl, v1.dom.value, v2.dom.value)
      && checkConv(lvl + 1, v1.codom.applyTo(currLvl), v2.codom.applyTo(currLvl))
    v1 is Value.VLam && v2 is Value.VLam -> checkConv(lvl, v1.body.applyTo(currLvl), v2.body.applyTo(currLvl))
    v1 is Value.VLam -> checkConv(lvl + 1, v1.body.applyTo(currLvl), Value.VApp(v2, currLvl))
    v2 is Value.VLam -> checkConv(lvl + 1, Value.VApp(v1, currLvl), v2.body.applyTo(currLvl))
    v1 is Value.VVar -> v2 is Value.VVar && v1.lvl == v2.lvl
    v1 is Value.VApp && v2 is Value.VApp -> checkConv(lvl, v1.v1, v2.v1) && checkConv(lvl, v1.v2.value, v2.v2.value)
    else -> false
  }
}
