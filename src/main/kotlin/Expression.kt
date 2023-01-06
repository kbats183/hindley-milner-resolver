data class P(val name: String)

sealed class Expression

data class Variable(val name: String) : Expression() {
    override fun toString() = name
}

// p q
data class Application(val p: Expression, val q: Expression) : Expression() {
    override fun toString() = "$p ($q)"
}

// lambda n. body
data class Lambda(val n: String, val body: Expression) : Expression() {
    override fun toString() = "\\$n . $body"
}

// let x = P in Q
data class Let(val x: String, val p: Expression, val q: Expression) : Expression() {
    override fun toString() = "let x = ($p) in ($q)"
}
