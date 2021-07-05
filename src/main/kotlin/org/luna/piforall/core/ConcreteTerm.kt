package org.luna.piforall.core


typealias CType = CTerm

sealed class CTerm {
    data class CVar(val name: Name) : CTerm()
    data class CLam(val binder: Name, val body: CTerm) : CTerm()
    data class CApp(val tm1: CTerm, val tm2: CTerm) : CTerm()
    data class CPi(val binder: Name, val dom: CType, val codom: CType) : CTerm()
    object CUniv : CTerm()
}

