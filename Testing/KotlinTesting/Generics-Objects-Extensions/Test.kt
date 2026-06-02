/**
 * Generics, objects, and extensions
 */

class FillInTheBlankQuestion(
    val questionText: String,
    val answer: String,
    val difficulty: String
)

class TrueOrFalseQuestion(
    val questionText: String,
    val answer: Int,
    val difficulty: String
)

class NumericQuestion(
    val questionText: String,
    val answer: Int,
    val difficulty: String
)

/*
The issue with the classes above is this: There should be a way to organize the classes potentially into one, but the 'answer' property's type
changes depending on the type of question. A parent class wouldn't solve it because then you'd have a class called Question() with no answer
property.

The solution is to use a generic data type.
*/

fun main() {

}
