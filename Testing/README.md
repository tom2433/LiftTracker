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

Void functions are indicated by ```Unit``` or you can just omit it:

```Kotlin
fun doAbsolutelyNothing(): Unit {
    println("e");
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

# Kotlin in Android Studio

## Composable functions

- Compose uses a declarative UI approach (focus on <em>what</em> the UI should look like rather than <em>how</em> to build it)
- <em>Everything</em> about how your UI should look should use Composable functions
- UI Composables are immutable and there is no way to change them once they have been created
- **Recomposition** - Since Composables are immutable, they cannot be changed. Instead, when the app data has changed (and thus the arguments for the function have changed), the UI refreshes and the Composable is automatically re-executed.
    - just call the composable function again to update the UI

### Naming conventions

- must be a noun
- must be PascalCase
- must not be a nouned proposition like ```TextFieldWithLink()```

### Composable annotation

- add ```@Composable``` annotation before function header
- composable function names are capitalized
- composable functions can't return anything

```Kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
```

> [!NOTE]
> Every composable function must include an optional ```modifier``` parameter like so:
> ```Kotlin
> @Composable
> fun AComposableFunction(modifier: Modifier = Modifier)
> ```

### Composable build in layouts

A Surface is one, Row or Column are some others:

```Kotlin
@Composable
fun Greeting() {
    Column {
        Text("Hello there")
        Text("General Kenobi")
    }
}
```

### Preview annotation

- tells android studio that this composable should be shown in the design view of this file
- takes in a boolean parameter called showBackground

```Kotlin
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GreetingCardTheme {
        Greeting("Bingus")
    }
}
```

---

## Surfaces

- container that represents a section of UI where you can alter the appearance, such as the background color or border
- default container is box layout, but you can change it to surface
- Surface() takes in a color parameter

```Kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Cyan) {
        Text(
            text = "Hello, $name!",
            modifier = modifier
        )
    }
}
```

> [!NOTE]
> Layouts like ```Box```, ```Row```, and ```Column``` use Trailing Lambda Syntax, which means that they use curly braces directly after the layout name instead of parentheses. Ex:
> ```Kotlin
> Box {
>   // UI components
> }
> ```

Change the ```setContent{}``` in ```onCreate()``` to also use a Surface:

```Kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme {
                // Surface container using background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorscheme.background
                ) {
                    Greeting("World")
                }
            }
        }
    }
}
```

### Colors

Color class has some preset values:

```Kotlin
Color.Black
Color.DarkGray
Color.Gray
Color.LightGray
Color.White
Color.Red
Color.Green
Color.Blue
Color.Yellow
Color.Cyan
Color.Magenta
Color.Transparent
Color.Unspecified           // transparent
```

Or you can create your own with rgb, hsl, or hsv:

```Kotlin
Color(0.5f, 0.5f, 0.5f)
```

---

## Padding

- ```Modifier``` - used to augment or decorate a composable
- ```padding``` - a modifier used to add space around the element (use ```Modifier.padding()```)
- Every Composable has an optional parameter of the type ```Modifier```. This is the first optional parameter.

Adding a padding to the modifier with a size of 24 dp:

```Kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color.Cyan) {
        Text(
            text = "Hi, my name is $name!", 
            modifier = modifier.padding(24.dp)
        )
    }
}
```

---

## Run on android device

Go [here](https://developer.android.com/codelabs/basic-android-kotlin-compose-connect-device?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-1-pathway-2%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-connect-device#2).

---

## Images

### Upload an image

- Go to View > Tool Windows > Resource Manager
- Click + for "import drawables"
- Upload and select Qualifier Type "Density" and select "No Density"
- Images are placed in the ```/app/src/main/res/drawable-nodpi``` folder to stop the resizing behavior

### Add an image as a composable

- Call the ```painterResource()``` function and pass in the resource ID. This function loads a drawable image resource and takes in the resource ID as an argument.
- Then add an ```Image``` composable and pass the Painter object.
- The ```contentDescription``` parameter is for accessibility like an alt text

Example:

```Kotlin
@Composable
fun GreetingImage(modifier: Modifier = Modifier) {
    val image = painterResource(R.drawable.background)
    
    Image(
        painter = image
        contentDescription = null
    )
}
```

### Additional Image Parameters

- contentScale: how to size the image (ContentScale.Crop will scale the image uniformly to maintain the aspect ratio so that the width and height are equal to or larger than the corresponding dimension of the screen)
- Sometimes the image won't actually fit the entire screen. In that event, set its modifier to Modifier.fillMaxSize() as well as its parent container

---

## Layout modifiers

List of some obvious modifiers:

```Kotlin
Modifier.background(color = Color.Green)
```

To arrange objects in a parent Row or Column:

```Kotlin
// to set childrens' position in a row, use horizontalArrangement and verticalAignment
// for column, use vertical Arrangment and horizontalAlignment

// for a column
verticalArrangement = Arrangement.SpaceBetween,
verticalArrangement = Arrangement.SpaceAround,
verticalArrangement = Arrangement.SpaceEvenly,
verticalArrangement = Arrangement.Top,
verticalArrangement = Arrangement.Center,
verticalArrangement = Arrangement.Bottom
horizontalAlignment = Alignment.Start,
horizontalAlignment = Alignment.End,
horizontalAlignment = Alignment.Center

// for a row
horizontalArrangement = Arrangement.SpaceBetween,
horizontalArrangement = Arrangement.SpaceAround,
horizontalArrangement = Arrangement.SpaceEvenly,
horizontalArrangement = Arrangement.End,
horizontalArrangement = Arrangement.Center,
horizontalArrangement = Arrangement.Start
verticalAlignment = Alignment.Top,
verticalAlignment = Alignment.Center,
verticalAlignment = Alignment.Bottom
```

Padding example usages:

```Kotlin
Modifier.padding(
    start = 16.dp,
    top = 16.dp,
    end = 16.dp,
    bottom = 16.dp
)
```
