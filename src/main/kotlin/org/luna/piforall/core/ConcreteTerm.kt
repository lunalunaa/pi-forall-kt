package org.luna.piforall.core

sealed class CTerm {
    data class CVar(val name: Name) : CTerm()
    data class CLam(val name: Name, val body: CTerm) : CTerm()
    data class CApp(val tm1: CTerm, val tm2: CTerm) : CTerm()
    data class CPi(val binder: Name, val dom: CTerm, val codom: CTerm) : CTerm()
    object CUniv : CTerm()
}
