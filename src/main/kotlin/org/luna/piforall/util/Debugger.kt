package org.luna.piforall.util

import org.luna.piforall.core.CTerm
import org.luna.piforall.core.Context
import org.luna.piforall.core.VType


/**
 * Prints type checking steps
 */
object Debugger {
  fun debugChecker(ctx: Context, ct: CTerm, expected: VType) {
    println("CHECKING MODE")
    println(ctx)
    println("checking $ct")
    println("against $expected")
    println("==================================================================")
  }

  fun debugInferer(ctx: Context, ct: CTerm) {
    println("INFER MODE")
    println(ctx)
    println("inferring $ct")
    println("==================================================================")
  }
}
