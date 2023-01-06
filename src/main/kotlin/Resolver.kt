data class ResolveResult(val gamma: Map<String, Type>, val mType: Type)

typealias TypeSubs = Map<String, Type>

private fun TypeSubs.applySubstitute(t: Type): Type {
    return when (t) {
        is Type.T -> this[t.name] ?: t
        is Type.A -> Type.A(t.variable, minus(t.variable).applySubstitute(t.e))
        is Type.L -> Type.L(applySubstitute(t.from), applySubstitute(t.to))
        else -> t
    }
}

private fun TypeSubs.applySubstitute(ts1: TypeSubs): TypeSubs {
    return ts1.entries.associate { it.key to ts1.applySubstitute(it.value) } + this
}

class ResolvingException(message: String) : Error(message)

fun occursIn(n: String, t: Type): Boolean {
    return when (t) {
        is Type.T -> t.name == n
        is Type.L -> occursIn(n, t.from) || occursIn(n, t.to)
        is Type.A -> n != t.variable && occursIn(n, t.e)
        else -> false
    }
}

fun unification(a: Type, b: Type): TypeSubs {
    return when {
        a is Type.P && b is Type.P && a.name == b.name -> emptyMap()
//        a is Type.T && b is Type.T && a.name == b.name -> emptyMap()
        a is Type.T -> if (occursIn(a.name, b)) {
            throw ResolvingException("Occurs check failed $a in $b")
        } else {
            return mapOf(a.name to b)
        }

        b is Type.T -> unification(b, a)
        a is Type.L && b is Type.L -> {
            unification(a.from, b.from).applySubstitute(unification(a.to, b.to))
        }

        else -> throw ResolvingException("Unification failed $a /= $b")
    }
}

class ResolverContext {
    private var variableCounter: Int = 0
    private fun nextTypeVar(): Type.T {
        return Type.T("_${variableCounter++}")
    }

    fun resolve(m: Expression, gamma: Map<String, Type>): Type {
        var (S, t) = resolveImpl(m, gamma)

        while (true) {
            val ins = (t.freeVariables() - S.keys).distinct()
            val newT = ins.fold(S.applySubstitute(t)) { acc, s -> Type.A(s, acc) }
            if (newT == t) {
                return t
            }
            t = newT
        }
    }

    private fun resolveImpl(m: Expression, gamma: Map<String, Type>): ResolveResult = when (m) {
        is Variable -> {
            val t = gamma[m.name] ?: throw ResolvingException("no type `${m.name}` in gamma")
            t.unwrapForall()
            ResolveResult(emptyMap(), t.unwrapForall())
        }

        is Lambda -> {
            val tau = nextTypeVar()
            val gamma_ = gamma.minus(m.n)
            val gamma__ = gamma_ + (m.n to tau)
            val (S_, tau_) = resolveImpl(m.body, gamma__)
            ResolveResult(S_, Type.L(S_.applySubstitute(tau), tau_))
        }

        is Application -> {
            val (S1, t1) = resolveImpl(m.p, gamma)
            val (S2, t2) = resolveImpl(m.q, S1.applySubstitute(gamma))

            val t = nextTypeVar()
            val S3 = unification(S2.applySubstitute(t1), Type.L(t2, t))
            ResolveResult(S3.applySubstitute(S2.applySubstitute(S1)), S3.applySubstitute(t))
        }

        is Let -> {
            val (S1, t1) = resolveImpl(m.p, gamma)
            val gamma_ = gamma - m.x
            val fv = t1.freeVariables()
            val gamma__ = gamma_ + (m.x to fv.fold(t1) { ac, v -> Type.A(v, ac) })
            val (S2, t2) = resolveImpl(m.q, S1.applySubstitute(gamma__))

            ResolveResult(S2.applySubstitute(S1), t2)
        }
    }.also {
        println("-- $m : ${it.gamma}")
        println("--- : ${it.gamma}")
    }

    private fun Type.unwrapForall(): Type = when (this) {
        is Type.A -> mapOf(variable to nextTypeVar()).applySubstitute(e).unwrapForall()
        else -> this
    }
}

private fun Type.freeVariables(): Set<String> {
    return when (this) {
        is Type.A -> {
            e.freeVariables() - this.variable
        }

        is Type.L -> {
            this.from.freeVariables() + this.to.freeVariables()
        }

        is Type.T -> {
            setOf(name)
        }

        else -> emptySet()
    }
}
