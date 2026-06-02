fun main() {
    // print("Enter a number: ")
    // val x = readLine()?.toInt() ?: 0

    // when (x) {
    //     1 -> println("One")
    //     2 -> println("Two")
    //     3 -> println("Three")
    //     4 -> println("Four")
    //     5 -> println("Five")
    //     else -> println("something else")
    // }

    val nums = listOf(1, 2, 3, 4)
    val doubled = nums.map { it * 2 }
    println(doubled)
}

