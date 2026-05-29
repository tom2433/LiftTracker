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

---

## Creating and storing state

```Kotlin
var count by remember { mutableStateOf(0) }
```

This is shorthand for creating and storing state that survives recompositions.

```remember``` stores a value in Compose memory.

- The value is kept between recompositions of the composable.
- Without ```remember```, the value would reset every time the UI redraws.

```mutableStateOf(0)``` creates observable state. When the value changes, Compose automatically recomposes any UI using it. See [using buttons](#state) for more info.

# Kotlin in Android Studio

For debugging tutorial, visit [this link](https://developer.android.com/codelabs/basic-android-kotlin-compose-intro-debugger?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-2-pathway-2%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-intro-debugger#1).

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

#### Material Design colors

All material design colors can be accessed using ```MaterialTheme.colorScheme.``` followed by a color, like ```MaterialTheme.colorScheme.primary```. Below are all of the material design color options and their usages:

- ```primary```
    - main brand/accent color, used for buttons, active controls, selected items, highlights
- ```onPrimary```
    - color used on top of primary; usually text/icons on a primary-colored surface, like white text on a blue button
- ```primaryContainer```
    - less intense version of primary; used for cards, selected chips, containers needing emphasis but not full primary strength
- ```onPrimaryContainer```
    - used for text/icons shown on primaryContainer
- ```secondary```
    - a secondary accent color; used less frequently than primary; used for alternate buttons, supporting accents
- ```onSecondary```
    - used for content shown on top of secondary
- ```secondaryContainer```
    - a softer secondary background/container color
- ```onSecondaryContainer```
    - used for content shown on top of secondaryContainer
- ```tertiary```
    - a third accent color for visual variety; often used sparingly for charts, special callouts, or decorative accents
- ```onTertiary```
    - used for content shown on top of tertiary
- ```tertiaryContainer```
    - soft tertiary container color
- ```onTertiaryContainer```
    - used for content shown on top of tertiaryContainer
- ```background```
    - used for the main app background color; changes based on light/dark mode
- ```onBackground```
    - used for text/icons displayed on the background
- ```surface```
    - the default color for surfaces and components, like cards, sheets, menus, and dialogs
- ```onSurface```
    - used for text/icons shown on surfaces
- ```surfaceVariant```
    - an alternative surface color; used for outlined components and lower emphasis sections
- ```onSurfaceVariant```
    - used for content shown on surfaceVariant
- ```error```
    - used for error states like invalid text fields or destructive actions
- ```onError```
    - used for content shown on error
- ```errorContainer```
    - soft error background
- ```onErrorContainer```
    - used for content shown on errorContainer
- ```outline```
    - used for borders and dividers, like text field borders and separators
- ```outlineVariant```
    - softer/lower emphasis outline
- ```surfaceTint```
    - used for tint applied to elevated surfaces; helps create Material 3 elevation effects
- ```inverseSurface```
    - opposite-tone surface color; used for things like snackbars and temporary overlays
- ```inverseOnSurface```
    - used for content shown on inverseSurface
- ```inversePrimary```
    - inverse version of primary, used in inverse-colored components
- ```scrim```
    - a semi-transparent overlay color; used behind dialogs, drawers, and modals

**Surface Container Colors for newer Material 3**

These create subtle elevation layering without manually changing colors:

- ```surfaceBright```
- ```surfaceDim```
- ```surfaceContainer```
- ```surfaceContainerLow```
- ```surfaceContainerLowest```
- ```surfaceContainerHigh```
- ```surfaceContainerHighest```

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

### Other useful image hacks

To wrap a surface around an image such that the surface is only as wide as its child (useful for adding a background to a transparent image), use ```Modifier.wrapContentWidth``` as follows:

```Kotlin
Surface(
    modifier = Modifier.wrapContentWidth
) {
    Image(
        // ...
    )
}
```

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

### Spacing between elements in a column

Use ```Arrangement.spacedBy()``` for the verticalArrangement parameter.

Ex:

```Kotlin
Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(
        4.dp,
        alignment = Alignment.CenterVertically
    ),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    // put elements here, they will automatically be spaced now
}
```

> [!NOTE]
> This can also work for rows I think? Just change the verticals and horizontals probably

### Text Alignment

```Kotlin
textAlign = TextAlign.Center
```

### Using wrapContentSize()

```wrapContentSize()``` specifies that the available space should at least be as large as the components inside of it. You can also use ```fillMaxSize()``` in combination with this in order to center align things. I know this doesn't make sense but it should somehow.

It's usually used with a composable that takes up the entire screen like so:

```Kotlin
@Preview(showBackground = true)
@Composable
fun DiceWithButtonAndImage(modifier: Modifier = Modifier
    .fillMaxSize()
    .wrapContentSize(Alignment.Center)
) {
    // put contents here, they will be centered in the middle of the screen I think
}
```

---

## Borders

BorderStroke class:

- Initialize as ```BorderStroke(Dp, Brush)```
- ```Dp``` can just be something like ```4.dp```
- ```Brush``` can just be something like ```Color.Red```

Ex:

```Kotlin
Surface(
    modifier = Modifier.fillMaxSize(),
    border = BorderStroke(4.dp, Color.Red)
) {
    // some things contained inside of a border
}
```

---

## Spacers

Used to make spacing more explicit. Takes ```Modifier``` as a parameter, can use ```Modifier.width```, ```Modifier.height```, and ```Modifier.size``` modifiers. Can also take ```Modifier.weight``` which is more helpful.

Ex:

```Kotlin
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Spacer(
        modifier = Modifier.weight(1f)
    )
    Text(
        text = "Placeholder text"
    )
    Spacer(
        modifier = Modifier.weight(1f)
    )
}
```

---

## State

### Creating and storing state for implementing button functionality

For variables to survive a refresh of UI (when a composable is redrawn), you need to store the state of that variable. This is done using something like this:

```Kotlin
var count by remember { mutableStateOf(0) }
```

This means that ```count``` starts as 0, and whenever ```count``` is updated, compose automatically refreshes anything that depends on it. You can then feel free to update count inside the composable directly:

```Kotlin
count++
```

You can also use this syntax:

```Kotlin
val result = remember { mutableStateOf(0) }
```

But that would mean that ```result``` is now a ```MutableState``` object, and you can only modify its value like this:

```Kotlin
result.value++
println(result.value)
```

The ```by``` just means that **property delegation** is used to delegate the value of the ```MutableState``` object to ```result```, so that it can be used directly. Also note that ```val``` is used here, meaning the reference to the object does not change, but the value inside it can.

The delegation version is used more frequently because it's easier to use and it doesn't make much sense to use the raw version.

### State for text field functionality

```TextField()```s must have ```value``` and ```onValueChange``` parameters. ```value``` is the initial value in the text field, and ```onValueChange``` is a lambda that contains functionality that executes when the value is changed. The obvious functionality that should be implemented is that the content in the text field should be updated as the user types:

```Kotlin
fun NormalTextField(modifier: Modifier = Modifier) {
    var inputtedText by remember { mutableStateOf("") }

    TextField(
        value = inputtedText,
        onValueChange = { inputtedText = it },      // where 'it' is the user's keyboard input
        modifier = modifier
    )
}
```

#### Other TextField parameters

The ```label``` parameter takes in a lambda which should contain a composable, like ```Text```. The ```singleLine``` parameter takes in a boolean indicating if the text field is one long scrollable line instead of multiple lines. ```keyboardOptions``` is important for the Lift Tracker since it can bring up the number keypad instead of just the text one. A number textfield might look like this:

```Kotlin
@Composable
fun EditNumberField(modifier: Modifier = Modifier) {
    var amountInput by remember { mutableStateOf("") }

    TextField(
        value = amountInput,
        onValueChange = {
            amountInput = it
        },
        label = {
            Text(
                text = stringResource(R.string.bill_amount)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
```

#### KeyBoardOptions

The ```KeyBoardOptions()``` constructor can also take in a ```imeAction``` parameter, which takes  in a ```ImeAction``` object. This changes what kind of button is shown in place of the "enter" key.

Usage:

```Kotlin
TextField(
    keyboardOptions = KeyboardOptions.Default.copy(
        keyBoardType = KeyboardType.Number,
        imeAction = ImeAction.Go
    )
)
```

Values:

- ```ImeAction.Search``` is used when the user wants to execute a search
- ```ImeAction.Send``` is used when the user wants to send the text in the input field
- ```ImeAction.Go``` is used when the user wants to navigate to the target of the text in the input
- ```ImeAction.Done``` is used when the user wants to complete a process. This also closes the keypad.

### State hoisting

In the above example, if an element in a parent container needed access to the value entered in the text field, we'd do **state hoisting**. This involves moving the ```amountInput``` value (the variable with the mutable state) up into the parent container, which also means that we must hoist the value passed to the ```value``` parameter of the ```TextField``` and the lambda passed to the ```onValueChange``` parameter of the ```TextField```.

This can be done like so:

```Kotlin
@Composable
fun ParentContainer(modifier: Modifier = Modifier) {
    // amountInput can now be accessed by parent container
    var amountInput by remember { mutableStateOf("") }

    EditNumberField(
        value = amountInput,
        onValueChange = {
            amountInput = it
        },
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun EditNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(R.string.bill_amount)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}
```.
