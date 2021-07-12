package org.luna.piforall.core


typealias CType = CTerm
typealias Program = List<CDecl>

sealed class CTerm {
    // TODO: create a debug mode for printing these terms?
    data class CVar(val name: Name) : CTerm() {
        override fun toString(): String = name
    }

    data class CLam(val binder: Name, val body: CTerm) : CTerm() {
        override fun toString(): String = "\\$binder. $body"
    }

    data class CApp(val tm1: CTerm, val tm2: CTerm) : CTerm() {
        override fun toString(): String = "$tm1 $tm2"
    }

    data class CPi(val binder: Name, val dom: CType, val codom: CType) : CTerm() {
        override fun toString(): String = "($binder: $dom) -> $codom"
    }

    object CUniv : CTerm() {
        override fun toString(): String = "U"
    }
}

data class CDecl(val name: Name, val sig: CType, val def: CTerm) {
    override fun toString(): String = "$name: $sig\n$name = $def"
}
