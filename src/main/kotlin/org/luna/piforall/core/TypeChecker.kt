package org.luna.piforall.core

class TypeChecker(debugMode: Boolean) {
    private val elaborator = Elaborator(debugMode)

    private fun typeCheck(tm: CTerm, tyEvaluated: VType): Term? =
        try {
            elaborator.checkTy(tm, tyEvaluated)
        } catch (e: TypeCheckError) {
            println(e.report())
            null
        }

    fun checkAndNormalize(tm: CTerm, ct: VType): Term? {
        val tmElaborated = typeCheck(tm, ct)
        return if (tmElaborated != null) {
            Normalizer.normalize(tmElaborated)
        } else null
    }

    fun typeCheckAgainstUniv(ty: CTerm): VType? = try {
        val tyElaborated = elaborator.checkTy(ty, Value.VUniv)
        Normalizer.eval(tyElaborated)
    } catch (e: TypeCheckError) {
        println(e.report())
        null
    }

    // TODO: this is not gonna work
    private fun checkDeclaration(decl: CDecl): CheckedDecl? {
        val tyVal = typeCheckAgainstUniv(decl.sig)
        return if (tyVal != null) {
            val tm = typeCheck(decl.def, tyVal)
            tm?.let { CheckedDecl(decl.name, tyVal, it) }
        } else {
            null
        }
    }

    // TODO: this is not gonna work
    fun checkDecls(decls: List<CDecl>): List<CheckedDecl>? {
        val checkedDecls = decls.map { checkDeclaration(it) }
        return if (checkedDecls.contains(null)) null else checkedDecls.filterNotNull()
    }
}