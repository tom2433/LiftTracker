import kotlin.math.round

fun convertDoubleTimeToTripleTime(minutes: Double): Triple<Int, Int, Double> {
    // assume that minutes is >= 0
    val resultHours: Int = minutes.toInt() / 60
    val resultMinutes: Int = minutes.toInt() % 60
    val resultSeconds: Double = (minutes - minutes.toInt()) * 60.0

    return Triple(resultHours, resultMinutes, resultSeconds)
}

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
    val valToRound: Double = 32.999999999999999
    val rounded: Double = round(valToRound * 100) / 100

    println(rounded.toString())
}
