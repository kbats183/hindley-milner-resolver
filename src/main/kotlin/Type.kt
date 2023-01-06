sealed class Type {
    //    object T : Type()
    data class P(val name: String) : Type() {
        override fun toString() = "P.$name"
    }

    data class T(val name: String) : Type() {
        override fun toString() = name
    }

    data class A(val variable: String, val e: Type):Type() {
        override fun toString() = "∀ $variable. $e"
    }

    data class L(val from: Type, val to: Type) : Type() {
        override fun toString() = "($from) -> ($to)"
    }
}
