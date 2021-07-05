package org.luna.piforall.core

sealed class TypeCheckError : Exception() {
    data class TypeMismatch(val expected: VType, val inferred: VType) : TypeCheckError() {
        override fun toString(): String {
            return "Type mismatch\n\nExpected:$expected\n\nInferred:$inferred"
        }
    }

    // TODO: write toString function for these terms
    data class VarOutScope(val v: CTerm.CVar) : TypeCheckError() {
        override fun toString(): String {
            return "Variable ${v.name} out of scope"
        }
    }

    data class ExpectedFunType(val inferred: VType) : TypeCheckError() {
        override fun toString(): String {
            return "Expected function type, but found $inferred"
        }
    }

    object CannotInferLambda : TypeCheckError() {
        override fun toString(): String {
            return "Cannot infer lambda"
        }
    }
}