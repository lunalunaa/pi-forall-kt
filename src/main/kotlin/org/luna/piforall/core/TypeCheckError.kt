package org.luna.piforall.core


// TODO: make error reporter able to determine source position
sealed class TypeCheckError : Exception() {

    abstract fun report(): String

    data class TypeMismatch(val expected: VType, val inferred: VType, val concrete: CTerm) : TypeCheckError() {
        override fun report(): String = "Expected: $expected\n\nInferred: $inferred\nWhen checking: $concrete"
    }

    // TODO: write toString function for these terms
    data class VarOutOfScope(val v: CTerm.CVar) : TypeCheckError() {
        override fun report(): String = "Variable $v out of scope"
    }

    data class ExpectedFunType(val inferred: VType) : TypeCheckError() {
        override fun report(): String = "Expected function type, but inferred $inferred"
    }

    object CannotInferLambda : TypeCheckError() {
        override fun report(): String = "Cannot infer lambda"
    }
}