package org.luna.piforall.core


// TODO: make error reporter able to determine source position
sealed class TypeCheckError : Exception() {

    abstract fun report(): String

    data class TypeMismatchError(val expected: VType, val inferred: VType, val concrete: CTerm) : TypeCheckError() {
        override fun report(): String =
            "Expected: $expected\n\nInferred: $inferred\nWhen checking: ${concrete.pretty()}"
    }

    data class VarOutOfScopeError(val v: CTerm.CVar) : TypeCheckError() {
        override fun report(): String = "Variable ${v.pretty()} out of scope"
    }

    data class ExpectedFunTypeError(val inferred: VType) : TypeCheckError() {
        override fun report(): String = "Expected function type, but inferred $inferred"
    }

    data class CannotInferLambdaError(val concrete: CTerm) : TypeCheckError() {
        override fun report(): String = "Cannot infer lambda ${concrete.pretty()}"
    }
}