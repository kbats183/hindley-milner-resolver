fun main(args: Array<String>) {
    val baseTypes = mutableMapOf(
        "1" to Type.P("int"), "true" to Type.P("bool"), "id" to Type.L(Type.P("x"), Type.P("x")),
        "iPlus" to Type.L(Type.P("int"), Type.L(Type.P("int"), Type.P("int")))
    )
    val iT = Variable("1")
    val bT = Variable("true")

    val l1 = Lambda("x", iT)
    val l2 = Lambda("x", Application(Application(Variable("iPlus"), Variable("1")), Variable("x")))
    val l3 = Lambda("x", Application(Application(Variable("iPlus"), Variable("x")), Variable("1")))

    val taskZero = Lambda("f", Lambda("x", Variable("x")))
    val taskPlus1 = Lambda(
        "a", Lambda(
            "f", Lambda(
                "x",
                Application(
                    Application(Variable("a"), Variable("f")),
                    Application(Variable("f"), Variable("x"))
                )
            )
        )
    )
    val taskPower = Lambda("m", Lambda("n", Application(Variable("n"), Variable("m"))))

    val taskTwo = Application(Variable("plus1"), Application(Variable("plus1"), Variable("zero")))
    val taskTwo2 = Lambda("f", Lambda("x", Application(Variable("f"), Application(Variable("f"), Variable("x")))))

    val taskE = Application(Application(Variable("power"), Variable("two")), Variable("two"))
    val taskE2 = Application(Application(Variable("power"), Variable("two2")), Variable("two2"))

    resolveSequence(
        listOf(
            "zero" to taskZero,
            "plus1" to taskPlus1,
            "two" to taskTwo,
            "power" to taskPower,
            "e" to taskE,
        )
    )
    resolveSequence(
        listOf(
//            "two2" to taskTwo2,
//            "power" to taskPower,
//            "e2" to taskE2,
        )
    )

//    test(taskPower, baseTypes)
//    test(Lambda("a", Lambda("f", Lambda("x", Application(Variable("f"), Variable("x"))))), baseTypes)
//    val tests = listOf(iT, bT, l1, l2, l3)
//    tests.forEach {
//        println(it)
//        val (S1, t1) = ResolverContex().resolve(it, baseTypes)
//        println("$t1 : $S1")
//    }
}

fun resolveSequence(program: List<Pair<String, Expression>>) {
    val ctx = ResolverContext()
    program.fold<Pair<String, Expression>, TypeSubs>(emptyMap()) { S1, (name, e) ->
        println("resolving $name = $e")
        val t1 = ctx.resolve(e, S1)
        println("- type is $t1")
        S1 + mapOf(name to t1)
    }
}

private fun test(x: Lambda, baseTypes: MutableMap<String, Type>) {
//    println(x)
    val t1 = ResolverContext().resolve(x, baseTypes)
    println("$t1")
}
