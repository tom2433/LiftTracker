# Kotlin

- modern java
- weirder
- null-safe
- concise
- interoperable with Java

---

## Immutable types

Declare using 'val':

```Kotlin
val name = "Tom"
```

---

## Mutable types

Declare using 'var':

```Kotlin
var name = "tom"
name = "Tom
```

---

## Types

Usually inferred automatically, but can be specified:

```Kotlin
val x: Int = 5
val y: Double = 3.14
val name: String = "Tom"
val isCool: Boolean = false
```

---

## String templates

Can be used instead of concatenation:

```Kotlin
val name = "Tom"
println("Hello $name")
```

Can be used for expressions:

```Kotlin
println("${2 + 2}")
```

---

## Input

```Kotlin
fun main() {
    print("Enter name: ")
    val name = readLine()

    println("Hello $name")
}
```

---

## If statements

```Kotlin
val x = 5

if (x > 3) {
    println("Big numba")
} else {
    println("litto numba")
}
```

### Conditional statements for return values

```Kotlin
val max = if (a > b) a else b
```

---

## Loops

### While loop

```Kotlin
var i = 0

while (i < 5) {
    println(i)
    i++
}
```

### Loops over collections

```Kotlin
for (n in nums) {
    println(n)
}
```

### For range loop

```Kotlin
for (i in 1..5) {
    println(i)
}
/*
prints:
1
2
3
4
5
*/
```

#### Downward

```Kotlin
for (i in 5 downTo 1) {
    println(i)
}
```

#### Step

```Kotlin
for (i in 0..10 step 2) {
    println(i)
}
```

---

## Functions

```Kotlin
fun add(a: Int, b: Int): Int {
    return a + b
}
```

### Short version

```Kotlin
fun add(a: Int, b: Int) = a + b
```

---

## Null safety

Kotlin <em>LOVES</em> null safety

Normal variables cannot be null:

```Kotlin
val name: String = "Tom"
name = null                 // this is illegal
```

### Nullable types

```?``` means nullable

```Kotlin
val name: String? = null    // this is not illegal 
```

### Safe calls

```Kotlin
val length = name?.length
```

If ```name == null```, then ```null``` is stored in length instead of having everything crash.
This is ok since ```val``` can also hold a nullable type if it needs to.

### Elvis operator

```Kotlin
val length = name?.length ?: 0
```

This means that if the result is not null, then it will assign the actual length to length. Otherwise, just use 0.

---

## Arrays and lists

### Immutable list

```Kotlin
val nums = listOf(1, 2, 3)
```

### Mutable list

```Kotlin
val nums = mutableListOf(1, 2, 3)

nums.add(4)
```

### Looping over a collection

```Kotlin
for (n in nums) {
    println(n)
}
```

---

## Classes

```Kotlin
class Person(
    val name: String,
    var age: Int
)

fun main() {
    val p = Person("Tom", 22)           // no constructor needed

    println(p.name)
}
```

---

## Data classes

Kotlin <em>LOVES</em> data classes.

```Kotlin
data class Lift(
    val name: String,
    val weight: Double
)
```

This automatically gives:

- ```toString()```
- ```equals()```
- ```copy()```
- getter functions

---

## When statements

Like switch but better I guess:

```Kotlin
when (x) {
    1 -> println("One")
    2 -> println("Two")
    3 -> println("Three")
    else -> println("Other")
}
```

---

## Collections AND lambdas

```Kotlin
val nums = listOf(1, 2, 3, 4)
val doubled = nums.map { it * 2 }
println(doubled)

val evens = nums.filter { it % 2 == 0 }
println(evens)
```
