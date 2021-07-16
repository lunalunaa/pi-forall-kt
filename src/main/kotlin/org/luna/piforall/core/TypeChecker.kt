package org.luna.piforall.core


class TypeChecker(private val debugMode: Boolean) {

    private fun typeCheck(tm: CTerm, tyEvaluated: VType, context: Context = Context.emptyContext()): Term? =
        try {
            val elaborator = Elaborator(context, debugMode)
            elaborator.checkTy(tm, tyEvaluated)
        } catch (e: TypeCheckError) {
            println(e.report())
            null
        }

    fun checkAndNormalize(tm: CTerm, ty: VType, context: Context = Context.emptyContext()): Term? {
        val tmElaborated = typeCheck(tm, ty, context)
        return if (tmElaborated != null) {
            Normalizer(context.env).normalize(tmElaborated)
        } else null
    }

    fun typeCheckAgainstUniv(ty: CTerm, context: Context = Context.emptyContext()): VType? =
        try {
            val elaborator = Elaborator(context, debugMode)
            val tyElaborated = elaborator.checkTy(ty, Value.VUniv)
            Normalizer(context.env).eval(tyElaborated)
        } catch (e: TypeCheckError) {
            println(e.report())
            null
        }

    private fun checkDecl(context: Context, decl: CDecl): CheckedDecl? {
        val tyVal = typeCheckAgainstUniv(decl.sig, context)
        return tyVal?.let { ty ->
            typeCheck(decl.def, ty, context)?.let { CheckedDecl(decl.name, ty, it) }
        }
    }

    // I think this can be done with a fold?
    fun checkDecls(decls: List<CDecl>): Pair<List<CheckedDecl>, Env>? {
        val checkedDecls = mutableListOf<CheckedDecl>()
        var context = Context.emptyContext()
        for (decl in decls) {
            val declChecked = checkDecl(context, decl)
            if (declChecked != null) {
                context = context.define(
                    declChecked.name,
                    lazy { Normalizer(context.env).eval(declChecked.def) },
                    lazy { declChecked.sig })
                checkedDecls.add(declChecked)
            } else {
                return null
            }
        }
        return checkedDecls to context.env
    }
}