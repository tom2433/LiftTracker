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
name = "Tom"
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

---

## Generic Data Types

Generic data types can be used when a property of a certain class may vary depending on what the class is implemented for. An example is for a quiz when you have questions that may be numeric, true/false, or fill-in-the-blank. You might define all three classes:

```Kotlin
class NumericQuestion(
    val questionText: String,
    val answer: Int,
    val difficutly: String
)

class TrueOrFalseQuestion(
    val questionText: String,
    val answer: Boolean,
    val difficulty: String
)

class FillInTheBlankQuestion(
    val questionText: String,
    val answer: String,
    val difficulty: String
)
```

Or you could just define one class to solve this:

```Kotlin
class Question<T>(
    val questionText: String,
    val answer: T,
    val difficulty: String
)

// usage:

fun main() {
    val question1 = Question<String>("Quoth the raven ___", "nevermore", "medium")
    val question2 = Question<Boolean>("The sky is green. True or False?", false, "easy")
    val question3 = Question<Int>("What is 2 + 2?", 4, "hard")
}
```

## Enum classes

Enum classes are used for data types that have a limited set of values. Think about cardinal directions, for example - they only have North, South, East, and West. This can be used with our quiz question example:

```Kotlin
enum class Difficulty {
    EASY, MEDIUM, HARD
}

class Question<T>(
    val questionText: String,
    val answer: T,
    val difficulty: Difficulty
)

// usage:

fun main() {
    val question1 = Question<String>("Quoth the raven ___", "nevermore", Difficulty.MEDIUM)
}
```

## Data classes

Data classes are classes that only contain data. Defining one as such allows the compiler to make certain assumptions and automatically implement some methods like ```toString()```. You can convert the question class into a data class like so:

```Kotlin
data class Question<T>(
    val questionText: String,
    val answer: T,
    val difficulty: Difficulty
)
```

Since this is now a data class, Kotlin can now display the class's properties when calling ```toString()```:

```
Question(questionText=Quoth the raven ___, answer=nevermore, difficulty=MEDIUM)
```

```toString()```, in addition to the following functions, are automatically implemented:

- ```equals()```
- ```hashCode()```
- ```componentN()```: ```component1()```, ```component2()```, etc.
- ```copy()```

> [!NOTE]
> A data class needs to have at least one parameter in its constructor, and all constructor parameters must be marked with ```val``` or ```var```. A data class also cannot be ```abstract```, ```open```, ```sealed```, or ```inner```.

---

## Singleton objects

Singleton objects are used for when a class will only have one instance, i.e. player stats in a game for one user; an object to access a remote data source like a database; authentication, where only one user should be logged in at a time. Singleton objects do not have constructors since you cannot create instances of them.

Syntax:

```Kotlin
object StudentProgress {
    var total: Int = 10
    var answered: Int = 3
}
```

Usage (```companion object```'s are objects that you can place inside of a class)

```Kotlin
class Quiz
```

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
>
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
>
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

All material design colors can be accessed using ```MaterialTheme.colorScheme.``` followed by a color, like ```MaterialTheme.colorScheme.primary```.

Primary colors are used for key components across the UI. Secondary colors are used for less prominent components. Tertiary colors are used for contrasting accents that can be used to balance primary and secondary colors or bring heightened attention to an element such as an input field.

The **on** color elements appear on top of other colors in the palette, and are primarily applied to text, iconography, and strokes.

Below are all of the material design color options and their usages:

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

#### Create your own Material theme

Go to the [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/) to create a palette. Export as Theme.kt. Replace the Theme.kt and Color.kt files in the project, and update the package names.

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
        painter = image,
        contentDescription = null
    )
}
```

### Additional Image Parameters

- contentScale: how to size the image (ContentScale.Crop will scale the image uniformly to maintain the aspect ratio so that the width and height are equal to or larger than the corresponding dimension of the screen)
- Sometimes the image won't actually fit the entire screen. In that event, set its modifier to Modifier.fillMaxSize() as well as its parent container

### Other useful image hacks

To wrap a surface around an image such that the surface is only as wide as its child (useful for adding a background to a transparent image), use ```Modifier.wrapContentWidth()``` as follows:

```Kotlin
Surface(
    modifier = Modifier.wrapContentWidth()
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
horizontalAlignment = Alignment.CenterHorizontally

// for a row
horizontalArrangement = Arrangement.SpaceBetween,
horizontalArrangement = Arrangement.SpaceAround,
horizontalArrangement = Arrangement.SpaceEvenly,
horizontalArrangement = Arrangement.End,
horizontalArrangement = Arrangement.Center,
horizontalArrangement = Arrangement.Start
verticalAlignment = Alignment.Top,
verticalAlignment = Alignment.CenterVertically,
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

### Scrollbars

To add scrolling functionality to a column, just add a ```verticalScroll()``` function to the modifier parameter like so:

```Kotlin
Column (
    modifier = Modifier.verticalScroll(rememberScrollState())
) {
    // column content here
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
```

---

## Icons

To use icons, import the following resources:

```Kotlin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
```

Icons can now be added like so, with a tint:

```Kotlin
Icon(
    imageVector = Icons.Rounded.AttachMoney,
    contentDescription = "Attach Money",
    tint = Color(4, 115, 65)
)
```

---

## LazyColumns (to display lists)

A ```LazyColumn``` composable can be used in place of a regular ```Column``` composable when you want to add content on demand, especially for long lists or when the length of the list is unknown. ```LazyColumn``` also provides scrolling by default.

To add items to the ```LazyColumn```, unlike the regular ```Column```, you add an ```items()``` method with a list as the argument, and you create a lambda function to add composables to the ```LazyColumn``` like so:

```Kotlin
fun AffirmationList(affirmationList: List<Affirmation>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(affirmationList) { affirmation ->
            AffirmationCard(
                affirmation = affirmation,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
```

> [!WARNING]
> Don't actually use lazy columns. They're terrible.

## Padding for lists

Use a LayoutDirection object to configure start and end padding for lists like so:

```Kotlin
@Composable
fun AffirmationsApp() {
    val layoutDirection = LocalLayoutDirection.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                start = WindowInsets.safeDrawing.asPaddingValues()
                    .calculateStartPadding(layoutDirection),
                end = WindowInsets.safeDrawing.asPaddingValues()
                    .calculateEndPadding(layoutDirection)
            )
    ) {
        AffirmationList(
            affirmationList = DataSource().loadAffirmations()
        )
    }
}
```

---

## How to use Scaffold

Scaffolds are like a structural blueprint to hold the top app bar, bottom navigation, and floating action button so you don't have to manually place them.

The minimum code you need to use a scaffold is below:

```Kotlin
Scaffold(
    modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
) { innerPadding ->
    Column(
        modifier = Modifier.padding(innerPadding)
    ) {
        // contents here
    }
}
```

To add a top app bar:

```Kotlin
Scaffold(
    topBar = {
        MyTopAppBar()
    },
    modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
) { innerPadding ->
    Column(
        modifier = Modifier.padding(innerPadding)
    ) {
        // contents here
    }
}
```

Create a top app bar:

```Kotlin
@Composable
fun MyTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row() {
                Image(
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                        .padding(8.dp),
                    painter = painterResource(R.drawable.walmart_logo),
                    contentDescription = null
                )
                Text(
                    text = "Walmar",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        },
        modifier = modifier
    )
}
```

---

## Animations

To animate the size of a container, add the ```animateContentSize()``` function to the ```Modifier``` parameter like so:

```Kotlin
Card() {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        // contents

        if (expanded) {
            // more contents
        }
    }
}
```

To animate the color of a container, use the ```animateColorAsState()``` function, assign it to a variable, and use that variable to define the color of a container like so:

```Kotlin
Card() {
    var expanded by remember { mutableStateOf(false) }
    val color by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        }
    )

    Column(
        modifier = Modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .background(color = color)
    )
}
```

> [!NOTE]
> When using ```animateColorAsState()```, you must assign it to a ```val``` variable.

### Transition Animations

Transition animations can be used to transform a card into a card detail page. This is accomplished via a ```SharedTransitionLayout() {}```, an ```AnimatedContent() {}``` composable, an ```AnimatedVisibilityScope``` object, a ```SharedTransitionScope``` object, the ```Modifier.sharedElement()``` function, and the ```rememberSharedContentState()``` function with a ```key``` String as an argument.

Keep in mind that the card and the card detail must have similar layouts, and it's best if they share the same major elemnts, like an image and a title.

Let's start with the ```SharedTransitionLayout() {}```. This must use an ```AnimatedContent() {}``` composable with a ```targetState``` and a ```label```. The following is an example from a sports app, which has cards that each correspond to a detail screen:

```Kotlin
Scaffold(
    topBar = {
        SportsAppBar(
            isShowingListPage = uiState.isShowingListPage,
            onBackButtonClick = { viewModel.navigateToListPag() },
            contentType = contentType
        )
    }
) { innerPadding ->
    // if being used with a smaller screen, it only shows the card list.
    // this is the only event in which the transition animation will be used
    if (contentType == SportsContentType.LIST_ONLY) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = uiState.isShowingListPage,
                label = "ContainerTransform"
            ) { showList -> // represents the targetState boolean
                if (showList) {
                    SportsList(
                        sports = uiState.sportsList,
                        currentSport = uiState.currentSport,
                        contentType = contentType,
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onClick = {
                            viewModel.updateCurrentSport(it),
                            viewModel.navigateToDetailPage()
                        },
                        contentPadding = innerPadding,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = dimensionResource(R.dimen.padding_medium),
                                start = dimensionResource(R.dimen.padding_medium),
                                end = dimensionResource(R.dimen.padding_medium)
                            )
                    )
                } else {
                    SportsDetail(
                        selectedSport = uiState.currentSport,
                        contentPadding = innerPadding,
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onBackPressed = {
                            viewModel.navigateToListPage()
                        }
                    )
                }
            }
        }
    } else {
        // when screen ratio is used for list and detail,
        // the transition animation is not applicable.
    }
}
```

As you can see above, both the ```AnimatedVisibilityScope``` and the ```SharedTransitionScope``` are both passed into the custom composables ```SportsList()``` and ```SportsDetail()```. These arguments are used in order to link the list elements with their corresponding detail screens. Let's start with ```SportsList()```:

```Kotlin
@Composable
private fun SportsList(
    sports: List<Sport>,
    currentSport: Sport,
    contentType: SportsContentType,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (Sport) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
        modifier = modifier
    ) {
        items(sports, key = { sport -> sport.id }) { sport ->
            with (sharedTransitionScope) {
                SportsListItem(
                    sport = sport,
                    onItemClick = onClick,
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState(key = sport.id.toString()),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                )
            }
        }
    }
}
```

Keep in mind that ```SportsListItem()``` is just a composable with only a ```Card() {}``` composable inside it that takes in the ```modifier``` argument passed to ```SportsListItem()``` as a parameter.

The ```SportsDetail()``` composable also takes in the ```SharedTransitionScope``` and ```AnimatedVisbilityScope``` objects to link itself to the appropriate cards:

```Kotlin
@Composable
private fun SportsDetail(
    selectedSport: Sport,
    onBackPressed: () -> Unit,
    contentPadding: PaddingValues,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackPressed()
    }
    val scrollState = rememberScrollState()
    val layoutDirection = LocalLayoutDirection.current

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .verticalScroll(state = scrollState)
                .padding(top = contentPadding.calculateTopPadding())
                .sharedElement(
                    rememberSharedContentState(key = selectedSport.id.toString()),
                    animatedVisibilityScope = animatedVisibilityScope
                )
        ) {
            // detail contents go here.
        }
    }
}
```

Keep in mind that the Modifier.sharedElement() function can be used on other containers like surfaces, columns, etc., as long as they are wrapped within a ```with(sharedTransitionScope) {}```.

## Accessibility

Go to [here](https://developer.android.com/codelabs/basic-android-kotlin-compose-test-accessibility?continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-3-pathway-3%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-test-accessibility#0) to learn about accessibility.

---

## Activity Life Cycle

Apps are supposed to be one activity which starts with the ```onCreate()``` method, and it changes state throughout the execution of the application.

### Life Cycle States

- **Initialized** - the app is opened and calls the ```onCreate()``` method
- **Created** - the ```onCreate()``` method has been called and the Activity has been created
- **Started** - the ```onStart()``` method is called to make the activity visible; or, the ```onRestart()``` method is called to make the activity visible again. ```onRestart()``` is not called every time the state tansitions between **Created** and **Started**. It is only called if ```onStop()``` was called (like when the user goes to their home screen) and the activity is subsequently restarted.
- **Resumed** - the ```onResume()``` method is called (even if it is being started for the first time), and the activity now has focus. Activity is still visible.
- Back to **Started** - the ```onPause()``` method is called, and the activity no longer has focus but is still visible.
- Back to **Created** - the ```onStop()``` method is called, and the activity is no longer visible.

- **Destroyed** - the ```onDestroy()``` method is called.

### ```Log``` class and the Logcat

The Logcat is the console for logging messages. A simple log instruction may look like this:

```Kotlin
// put this line at the top of the file, underneath the import statements, before the MainActivity class
private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // log the status with the TAG
        Log.d(
            tag = TAG,
            msg = "onCreate Called"
        )

        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    LiftTrackerApp()
                }
            }
        }
    }
}
```

Log instructions have a priority. In the above case, ```Log.d()``` logs debug messages, ```Log.v()``` logs verbose messages, ```Log.i()``` logs informational messages, ```Log.w()``` logs warning messages, and ```Log.e()``` logs error messages. The ```tag``` parameter is a string the lets you more easily find your log messages in the Logcat, and is typically the name of the class. Click the Logcat tab on the bottom left of the IDE to view these messages during runtime, and use the search function to search for the tags by typing ```tag:MainActivity```.

You can also override other methods when logging:

```Kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {    // creates the app
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent{
            // ...
        }
    }

    override fun onStart() {        // makes the app visible on screen, not interactable
        super.onStart()
        Log.d(TAG, "onStart Called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart Called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause Called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDesto")
    }
}
```

### How to save values across configuration changes

Sometimes when a configuration changes (i.e., the user rotates their device from portrait mode into landscape mode), the activity is shut down and then re-created. This resets all values back to default. To avoid this, use the ```rememberSaveable``` function in place of the ```remember``` function like so:

```Kotlin
// old version
var revenue by remember { mutableIntStateOf(0) }

// change to this:
var revenue by rememberSaveable { mutableIntStateOf(0) }
```

### ViewModels

A ```ViewModel``` is like the operational brain - it acts as a bridge between the raw app data and the visual UI layouts that the user interacts with. Compose is good at rendering layout but it is bad at remembering data long term, which is where the ViewModel comes in.

ViewModels keep data safely cached in memory during configuration changes, etc. to avoid using a million ```remember``` functions that may not even work.

To use a ```ViewModel```, add this to the ```libs.versions.toml```:

```TOML
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose" }
```

... and add this to the ```build.gradle.kts (Module :app)``` file's dependency section:

```Kotlin
implementation(libs.androidx.lifecycle.viewmodel.compose)
```

Create a ```ViewModel``` like so. This is an example from a word unscrambler game app:

```Kotlin
package com.example.lifttracker.ui

import androidx.lifecycle.ViewModel
import com.example.lifttracker.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Game UI state
private val _uiState = MutableStateFlow(GameUiState)

class GameViewModel : ViewModel() {
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private lateinit var currentWord: String
    private var usedWords: MutableSet<String> = mutableSetOf()

    private fun pickRandomWordAndShuffle(): String {
        // ...
    }

    private fun shuffleCurrentWord(word: String): String {
        // ...
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    init {
        resetGame()
    }
}
```

The GameUiState is defined in a different file, also in ```com.example.lifttracker.ui```:

```Kotlin
package com.example.lifttracker.ui

data class GameUiState(
    cal currentScrambledWord: String = ""
)
```

The currently scrambled word can be accessed in the UI like so:

```Kotlin
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(
    gameViewModel: GameViewModel = viewModel()
) {
    val gameUiState by gameViewModel.uiState.collectAsState()

    Text(
        text = gameUiState.currentScrambledWord,
        style = MaterialTheme.typography.displayMedium
    )
}
```

**Why not just use a normal class for ```GameViewModel```?**

With ```: ViewModel()```, the android framework recognizes this class as a special lifecycle aware component. When the activity is destroyed during a configuration update like a rotation, Android retains the ViewModel in memory, and when the activity recreates itself, it hooks back up to the exact same instance of this class.

**Text fields can be updated using a GameViewModel as well:**

In UI:

```Kotlin
OutlinedTextField(
    value = gameViewModel.userGuess,
    singleLine = true,
    shape = shapes.large,
    modifier = Modifier.fillMaxWidth(),
    colors = TextFieldDefaults.colors(
        focusedContainerColor = colorScheme.surface,
        unfocusedContainerColor = colorScheme.surface,
        disabledContainerColor = colorScheme.surface
    ),
    onValueChange = { gameViewModel.updateUserGuess(it) },
    label = {
        if (gameUiState.isGuessedWordWrong) {
            Text(stringResource(R.string.wrong_guess))
        } else {
            Text(stringResource(R.string.enter_your_word))
        }
    },
    isError = gameUiState.isGuessedWordWrong,
    keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
        onDone = { gameViewModel.checkUserGuess }
    )
)
```

In GameViewModel:

```Kotlin
private val _uiState = MutableStateFlow(GameUiState())

class GameViewModel : ViewModel() {
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private lateinit var currentWord: String
    private var usedWords: MutableSet<String> = mutableSetOf()
    var userGuess by mutableStateOf("")
        private set

    private fun pickRandomWordAndShuffle(): String {
        // ...
    }

    private fun shuffleCurrentWord(word: String): String {
        // ...
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // user guess is right. update score
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            // user guess is wrong. show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }

        // reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                isGuessedWordWrong = false,
                currentScrambledWord = pickRandomWordAndShuffle(),
                score = updatedScore,
                currentWordCount = currentState.currentWordCount.inc()
            )
        }
    }

    fun resetGame() {
        // ...
    }

    init {
        // ...
    }
}
```

In GameUiState:

```Kotlin
data class GameUiState(
    val currentScrambledWord: String = "",
    val isGuessedWordWrong: Boolean = false,
    val score: Int = 0,
    val currentWordCount: Int = 1,
)
```

---

## Nav Hosts

```NavHost``` objects can be used for navigating through pages in an application. The screens are represented with string variables, which ideally are further represented with enums. The ```NavHost``` object takes in a ```NavController```, a start destination (represented by a string), and a modifier as its parameters. A navhost will stack each screen on top of the previous one as they are introduced. You generally want to avoid passing the ```NavController``` around as it holds a lot of power. This is why we state hoist.

An example of how to use the ```NavHost``` is below. This example is for a CupcakeApp:

```Kotlin
enum class CupcakeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Flavor(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary)
}

@Composable
fun CupcakeApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = CupcakeScreen.valueOf(
        backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
    )

    Scaffold(
        topBar = {
            CupcakeAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = CupcakeScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Start Order Screen
            composable(route = CupcakeScreen.Start.name) {
                StartOrderScreen(
                    quantityOptions = DataSource.quantityOptions,
                    onNextButtonClicked = {
                        viewModel.setQuantity(it)
                        navController.navigate(CupcakeScreen.Flavor.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }

            // Select Flavor Screen (which is an instance of the SelectOption Screen)
            composable(route = CupcakeScreen.Flavor.name) {
                val context = LocalContext.current
                SelectOptionScreen(
                    subtotal = uiState.price,
                    onNextButtonClicked = {
                        navController.navigate(CupcakeScreen.Pickup.name)
                    },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    options = DataSource.flavors.map { id -> context.resources.getString(id) },
                    onSelectionChanged = { viewModel.setFlavor(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            // Pickup date screen (which is an instance of the SelectOption Screen)
            composable(route = CupcakeScreen.Pickup.name) {
                SelectOptionScreen(
                    subtotal = uiState.price,
                    onNextButtonClicked = {
                        navController.navigate(CupcakeScreen.Summary.name)
                    },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    options = uiState.pickupOptions,
                    onSelectionChanged = { viewModel.setDate(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            // Order Summary Screen
            composable(route = CupcakeScreen.Summary.name) {
                OrderSummaryScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    onSendButtonClicked = { subject: String, summary: String ->
                        // here we can use the subject and summary variables.
                        // this will also execute an intent, which is discussed further below.
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(
        route = CupcakeScreen.Start.name,
        inclusive = false
    )
}
```

## Intents

An intent is a request for the system to perform some action, commonly presenting a new activity. There are many different [intents](https://developer.android.com/guide/components/intents-filters) but we'll be looking at ```ACTION_SEND```:

```Kotlin
fun makeAnIntent(subject: String, summary: String) {
    val context = LocalContext.current

    shareOrder(
        context = context,
        subject = subject,
        summary = summary
    )
}

fun shareOrder(
    context: Context,
    subject: String,
    summary: String
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}
```

## Dynamic navigation for adapting to different screen ratios

Here we'll cover how to use 3 different types of navigation for 3 different types of screen aspect ratios. The three types of navigation are ```BOTTOM_NAVIGATION```, ```NAVIGATION_RAIL```, and ```PERMANENT_NAVIGATION_DRAWER```. These should be declared as enums in a file called ```WindowStateUtils.kt```. This example was taken from the Reply app practice:

```Kotlin
package com.example.lifttracker.ui.utils

enum class ReplyNavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER
}
```

In ```MainActivity.kt```, determine the window size like so:

```Kotlin
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme {
                val layoutDirection = LocalLayoutDirection.current

                Surface(
                    modifier = Modifier
                        .padding(
                            start = WindowInsets.safeDrawing.asPaddingValues()
                                .calculateStartPadding(layoutDirection),
                            end = WindowInsets.safeDrawing.asPaddingValues()
                                .calculateEndPadding(layoutDirection)
                        ),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val windowSize = calculateWindowSizeClass(this)
                    ReplyApp(windowSize = windowSize.widthSizeClass)
                }
            }
        }
    }
}
```

This window size is passed to the ```ReplyApp``` composable. We can then check the navigation type here like so:

```Kotlin
fun ReplyApp(
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    // ...

    val navigationType: ReplyNavigationType = when (windowSize) {
        WindowWidthSizeClass.Compact -> {
            ReplyNavigationType.BOTTOM_NAVIGATION
        }
        WindowWidthSizeClass.Medium -> {
            ReplyNavigationType.NAVIGATION_RAIL
        }
        WindowWidthSizeClass.Expanded -> {
            ReplyNavigationType.PERMANENT_NAVIGATION_DRAWER
        }
        else -> {           // keep the bottom navigation as a default
            ReplyNavigationType.BOTTOM_NAVIGATION
        }
    }
}
```

Now that the ```navigationType``` has been defined, it is now passed to the ```ReplyHomeScreen()``` composable. Inside ```ReplyHomeScreen()```, we check to see if the ```navigationType``` is a permanent navigation drawer and if the user is on the home screen. If both are the case, then we implement it right away, putting the ```ReplyAppContent()``` composable inside while passing the ```navigationType``` here too.

Otherwise, we check to see if the user is on the homepage, and if they are, then we simply just call the ```ReplyAppContent()``` composable while passing the ```navigationType``` in, otherwise, we pull up the ```ReplyDetailsScreen()``` composable since the user must have clicked an email to read. The reason we check for the permanent navigation drawer here is because the other navigation types do not require the app content to be passed into it.

Here is how the ```PermanentNavigationDrawer()``` composable is implemented. Note that some things here are undefined, but this is the general structure for learning purposes:

```Kotlin
PermanentNavigationDrawer(
    drawerContent = {
        PermanentDrawerSheet(Modifier.width(dimensionResource(R.dimen.drawer_width))) {
            // inside components of the navigation drawer here.
            Column() {
                // navigation drawer header here
                // ...

                for (navItem in navigationItemContentList) {
                    NavigationDrawerItem(
                        selected = selectedDestination == navItem.MailboxType,
                        label = {
                            Text(
                                text = navItem.text,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.text
                            )
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent
                        ),
                        onClick = { onTabPressed(navItem.mailboxType) }
                    )
                }
            }
        }
    }
) {
    ReplyAppContent(
        navigationType = navigationType,
        replyUiState = replyUiState,
        onTabPressed = onTabPressed,
        onEmailCardPressed = onEmailCardPressed,
        navigationItemContentList = navigationItemContentList,
        modifier = modifier
    )
}
```

Here is the ```ReplyHomeScreen()``` composable, which begins checking the navigation type:

```Kotlin
@Composable
fun ReplyHomeScreen(
    navigationType: ReplyNavigationType,
    replyUiState: ReplyUiState,
    onTabPressed: (MailboxType) -> Unit,
    onEmailCardPressed: (Email) -> Unit,
    onDetailScreenBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItemContentList = listOf(
        NavigationItemContent(
            mailboxType = MailboxType.Inbox,
            icon = Icons.Default.Inbox,
            text = stringResource(id = R.string.tab_inbox)
        ),
        ...
    )

    // implement navigation drawer
    if (navigationType == ReplyNavigationType.PERMANENT_NAVIGATION_DRAWER
        && replyUiState.isShowingHomepage
    ) {
        // navigation drawer (code above) goes here
    } else {
        // only show if homepage is showing
        if (replyUiState.isShowingHomepage) {
            ReplyAppContent(
                navigationType = navigationType,
                replyUiState = replyUiState,
                onTabPressed = onTabPressed,
                onEmailCardPressed = onEmailCardPressed,
                navigationItemContentList = navigationItemContentList,
                modifier = modifier
            )
        } else {
            ReplyDetailsScreen(
                replyUiState = replyUiState,
                onBackPressed = onDetailScreenBackPressed,
                modifier = modifier
            )
        }
    }
}
```

In the event that the navigation drawer is not supposed to be shown, the ```navigationType``` parameter is passed from ```ReplyHomeScreen()``` to ```ReplyAppContent()``` since the other navigation types can be a part of this content. The ```ReplyDetailsScreen()``` doesn't have to worry about this since it is independent of the navigation. It only includes a back button.

In the ```ReplyAppContent()``` composable, we use ```AnimatedVisibility()``` composables to decide whether we show or hide certain navigation types. You can see how this is implemented here:

```Kotlin
@Composable
private fun ReplyAppContent(
    navigationType: ReplyNavigationType,
    replyUiState: ReplyUiState,
    onTabPressed: ((MailboxType) -> Unit),
    onEmailCardPressed: (Email) -> Unit,
    navigationItemContentList: List<NavigationItemContent>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // set navigation rail visible if that is the navigation type
        AnimatedVisibility(visible = navigationType == ReplyNavigationType.NAVIGATION_RAIL) {
            NavigationRail(
                modifier = Modifier.testTag("Navigation Rail")
            ) {
                for (navItem in navigationItemContentList) {
                    NavigationRailItem(
                        selected = currentTab == newItem.mailboxType,
                        onClick = { onTabPressed(navItem.mailboxType) },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.text
                            )
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            // rest of app content goes here
            // this will display no matter what the navigation type is
            // ...

            // now set the bottom navigation bar visible only if that is the navigation type
            AnimatedVisibility(
                visible = navigationType == ReplyNavigationType.BOTTOM_NAVIGATION
            ) {
                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                    for (navItem in navigationItemContentList) {
                        NavigationBarItem(
                            selected = currentTab == navItem.mailboxType,
                            onClick = { onTabPressed(navItem.mailboxType) },
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.text
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
```

---

## Toast

A Toast is a very small popup that displays some text. It can be called very easily like so:

```Kotlin
val context = LocalContext.current
val text = "This is a toast"

Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
```

You can also create it with a string parameter to call it later, with varying strings:

```Kotlin
val context = LocalContext.current

val displayToast = { text: String ->
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

displayToast("Here is a toast popup")
displayToast("Here is another toast popup")
```

---

## Changing Content layout based on screen ratio/size

See SportsApp branch.

---

## Persist Data with Room

Room has three main components:

- Room entities: represent tables in your app's database. Can update existing rows and create new rows.
- Room DAOs (Data Access Objects): provide methods that your app uses to retrieve, update, insert, and delete data in the database.
- Room Database class: database class that provides your app with instance of the DAOs associated with that database.

So, the Room Database Class is farthest away from your code but it provides the DAOs, and the DAOs provide you with the entities. Keep in mind that all of these are located in the ```com.example.lifttracker.data``` package.

### Add Room dependency to project

The process of adding the dependency for Room may change in the future. Consult [this link](https://developer.android.com/jetpack/androidx/releases/room) for any updates.

To add Room to your project, add this line to the ```build.gradle.kts (Project: Lift_Tracker)``` under the ```plugins``` section:

```kts
alias(libs.plugins.ksp) apply false
```

Then add this line to the ```build.gradle.kts (Module :app)``` under the ```plugins``` section:

```kts
alias(libs.plugins.ksp)
```

And then add these lines to the ```libs.versions.toml``` file under the ```versions``` section:

```toml
room = "2.6.1"
ksp = "2.2.10-2.0.2"
```

Add these lines to the ```libs.versions.toml``` file under the ```libraries``` section:

```toml
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
```

And finally add this line to the ```libs.versions.toml``` file under the ```plugins``` section:

```toml
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### How to create an Entity class

As mentioned, entities represent tables in your app's database. here is a sample entity. See how it is denoted with ```@Entity``` and contains an auto-generated primary key:

```Kotlin
@Entity(tableName = "items")    // the tableName is optional and will default to the class name
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val quantity: Int
)
```

### How to create the Data Access Object (DAO)

As mentioned, the DAO separates the persistence layer from the rest of the application by providing an abstract interface.

A DAO is an interface, so it should be defined like so, with the ```@Dao``` annotation:

```Kotlin
@Dao
interface ItemDao {
}
```

The annotations to use when writing functions for this interface are ```@Insert```, ```@Update```, ```@Delete```, and ```@Query```. These functions are also defined using ```suspend```, which lets the function run on a separate thread.

The onConflict argument tells the Room what to do in case of a conflict. Look [here](https://developer.android.com/reference/androidx/room/OnConflictStrategy.html) for the ```OnConflictStrategy``` documentation. Here are some examples on how to implement these functions:

```Kotlin
@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    // the entity that's updated has the same primary key as the entity that's passed in.
    // you can update some or all of the entity's other properties.
    @Update
    suspend fun update(item: Item)

    // @Delete annotation deletes an item or a list of items.
    // You need to pass the entities you want to delete
    // If you don't have the entity, you might have to fetch it before calling the delete() function.
    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItem(id: Int): Flow<Item>

    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<Item>>
}
```

Notice the ```:id``` in the query. This references the ```id``` parameter in ```getItem()```. The ```Flow``` return type gives a notification in some way whenever the data in the database changes. This allows you to observe the data and update your UI accordingly. The ```Flow``` return type also allows the query to run on the background thread, which is why you don't need to explicitly make it a ```suspend``` function and call it inside a coroutine scope.

### How to create a Room Database class

The ```RoomDatabase``` class defines the list of entities and DAOs and provides the app with instances of DAOs that you define. Then the app can use the DAOs to retrieve data from the database as instances of the associated data entity objects, and also use the data entities to update rows from the corresponding tables or create new rows for insertion.

Here is an example of the ```RoomDatabase``` class being implemented:

```Kotlin
/**
 * Database class with a singleton Instance object.
 */
@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var Instance: InventoryDatabase? = null
        fun getDatabase(context: Context): InventoryDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, InventoryDatabase::class.java, "item_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
```

A few notes:

- The ```abstract fun itemDao(): ItemDao``` is what links the database to the DAO.
- In the ```@Database()``` annotation:
    - the ```entities``` parameter specifies the data class entities (tables) in the database
    - the ```version``` parameter increases whenever the schema of the database table is changed
    - the ```exportSchema``` parameter indicates whether to keep schema version history backups
- The ```companion object {}``` allows access to the methods to create or get the database and uses the class name as the qualifier.
    - The ```Instance``` variable keeps only one reference to the database once it has been created, which helps to maintain a single instance of the database opened at any given time.
    - The ```@Volatile``` annotation means that the ```Instance``` variable is never cached, so all reads and writes are to and from the main memory, so that the value of ```Instance``` is always up to date. Changes made by one thread to ```Instance``` are immediately visible to all other threads.
- Inside the ```getDatabase()``` function, a ```synchronized{}``` block is used to ensure that only one thread can enter this block of code at a time, which makes sure that the database only gets initialized once.

### How to use the Room Database, Entities, and DAOs in practice

Consider banging your head against a well before we begin. Then create one single class that wraps the DAO to perform its functions. In this example (the InventoryApp), we make an interface for a "repository" class and then implement it like so:

**The Interface**

```Kotlin
interface ItemsRepository {
    /**
     * Retrieve all the items from the given data source.
     */
    fun getAllItemsStream(): Flow<List<Item>>

    /**
     * Retrieve an item from the given data source that matches with the id.
     */
    fun getItemStream(id: Int): Flow<Item?>

    /**
     * Insert item in the data source
     */
    suspend fun insertItem(item: Item)

    /**
     * Delete item form the data source
     */
    suspend fun deleteItem(item: Item)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(item: Item)
}
```

**The Implementation**

```Kotlin
class OfflineItemsRepository(private val itemDao: ItemDao) : ItemsRepository {
    override fun getAllItemsStream(): Flow<List<Item>> = itemDao.getAllItems()
    override fun getItemStream(id: Int): Flow<Item?> = itemDao.getItem(id)
    override suspend fun insertItem(item: Item) = itemDao.insert(item)
    override suspend fun deleteItem(item: Item) = itemDao.delete(item)
    override suspend fun updateItem(item: Item) = itemDao.update(item)
}
```

This interface and class are defined as separate files in the ```data``` package.

#### What the hell is a repository?

I'm glad you asked so politely. Repositories are useful when your app has multiple data sources. The ```ViewModel``` talks with the ```Repository```, and the ```Repository``` talks with the ```Room```. If there are multiple data sources, the ```Repository``` will talk with all of those, and the ```ViewModel``` will still just have to talk with the ```Repository```.

So, just call ```OfflineItemsRepository.getAllItems()``` right? No.

This example application uses dependency injection. That's why the ```OfflineItemsRepository``` needs an ```ItemDao``` to function, and why the **AppContainer** and **AppDataContainer** exist (to provide the ```ItemDao``` to the ```OfflineItemsRepository```). ```AppContainer.kt```, located in the ```data``` package, contains the manual dependency injection setup. The ```AppContainer``` interface provides a place where shared dependencies live, and one of those dependencies is ```itemsRepository```:

```Kotlin
/**
 * App Container for dependency injection.
 */
interface AppContainer() {
    val itemsRepository: ItemsRepository
}

/**
 * AppContainer implementation that provides an instance of OfflineItemsRepository
 */
class AppDataContainer(private val context: Context) : AppContainer {
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(
            itemDao = InventoryDatabase.getDatabase(context).itemDao()
        )
    }
}
```

A few notes:
- The ```ItemsRepository``` is defined ```by lazy``` so that it is not created until it is needed.
- So when someone asks for the ```itemsRepository```, ```AppDataContainer``` will:
    - get the room database
    - get the DAO from the database
    - create an ```OfflineItemsRepository``` using that DAO, and continue to use that repository afterward.
- All of this seems unnecessarily complicated because it is using **dependency injection**, which is a good coding practice where a class depends on an object, but it doesn't create that object itself.
    - An ```OfflineItemsRepository``` is built from the ```ItemsRepository``` and depends on an ```ItemDao```, but it doesn't create an ```ItemDao```. The ```RoomDatabase``` does.

So, just call ```AppDataContainer.itemsRepository.getAllItems()``` right? Still no.

The Application itself depends on an ```AppContainer``` since the ```AppDataContainer``` needs a ```Context```, but the compiler doesn't know that yet. There's a hidden ```Application()``` class in the background. So let's override it in the original package ```com.example.lifttracker```:

```Kotlin
class InventoryApplication : Application() {
    /**
     * AppContainer instance used by the rest of the classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(
            context = this
        )
    }
}
```

Having a custom ```Application``` now is all fine and dandy, but the compiler doesn't know that it exists yet, so we have to tell it that it does in the ```AndroidManifest.xml``` file.

To do this, we add ```android:name=".InventoryApplication"``` to the ```<application>``` tag in ```AndroidManifest.xml```. Inside the ```<application>``` tag exists an ```<activity>``` tag, which also has an ```android:name``` attribute. Now that the Android Manifest is updated, the startup order will look something like this:

1. Android starts the app process.
2. Android looks at ```<application android:name=".InventoryApplication">```.
3. Android creates ```InventoryApplication```.
4. ```InventoryApplication.onCreate()``` runs.
5. The ```AppDataContainer``` gets created.
6. Android launches ```MainActivity```.
7. ```MainActivity.onCreate()``` runs.
8. Compose UI starts.
9. UI asks for ViewModels.
10. ViewModels can access the repository through the ```InventoryApplication```'s ```AppDataContainer```.

All of the above might make sense except for the last part. How does a viewmodel even access the ```InventoryApplication``` in order to retreive the ```OfflineItemsRepository```?

#### How does a ViewModel access the custom Application class?

Thought things were already unnecessarily complicated? It's actually a lot worse than you think! A ```ViewModel``` can't actually access the ```InventoryApplication``` on its own, so it needs a ```ViewModelFactory``` to hold its pathetic, useless little hand. Here's how it works:

Let's say we want a ```ViewModel``` to have access to an ItemsRepository, so we define it like so inside the ui package:

```Kotlin
class ItemEntryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {
    // Item UI state
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState = ItemUiState(
            itemDetails = itemDetails,
            isEntryValid = validateInput(itemDetails)
        )
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }

    suspend fun saveItem() {
        if (validateInput()) {
            // remember that Item is an entity (row in a table),
            // so uiState.itemDetails must be converted to that
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }
}

data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)

data class ItemDetails(
    val id: Int,
    val name: String = "",
    val price: String = "",
    val quantity: String = ""
)
```

But where the flying fuck do we get an ```ItemsRepository``` to give to an ```ItemEntryViewModel```? The answer lies in a ```ViewModelFactory```, which is responsible for injecting repositories into ```ViewModel```s.

#### View Model Factories

The purpose of a ```ViewModelFactory``` is to provide instances of ```ViewModel```s, which becomes necessary when a ```ViewModel``` requires constructor arguments (like repositories).

You can create a View Model Factory inside a View Model Provider like so in the ui package. Keep in mind that ```CreationExtras``` is a container of objects provided by android, and it exists only during the creation process. It stores things like ```Application```, ```SavedStateRegistryOwner```, ```ViewModelStoreOwner```, etc. This is why it can access the ```InventoryApplication```. Don't worry about it.

```Kotlin
/**
 * Provides a factory to create instances of ViewModel for the entire app.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        /**
         * An initializer is a function that is executed with a CreationExtras as its receiver.
         * It returns a ViewModel.
         */

        // initializer for ItemEditViewModel
        initializer {
            ItemEditViewModel(
                savedStateHandle = this.createSavedStateHandle()
            )
        }

        // initializer for ItemEntryViewModel
        initializer {
            ItemEntryViewModel(inventoryApplication().container.itemsRepository)
        }

        // initializer for ItemDetailsViewModel
        initializer {
            ItemDetailsViewModel(
                savedStateHandle = this.createSavedStateHandle()
            )
        }

        // initializer for HomeViewModel
        initializer {
            HomeViewModel()
        }
    }
}

/**
 * This is an extension function on CreationExtras.
 * That means that it lets any CreationExtras object call it as if this function belongs to it.
 * Since initializer is executed with a CreationExtras as its receiver, it can also use this function.
 */
fun CreationExtras.inventoryApplication(): InventoryApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as InventoryApplication)
```

Read the comments in the above code to understand it better. An ```initializer``` is a type of function that is executed with a ```CreationExtras``` as its receiver and returns a ```ViewModel```. You can see this in its source code if you really feel like looking at it.

Are we done? No.

We need to figure out how to create a view model in a UI screen, but we can't create it directly. We need to use the ```AppViewModelProvider```. Also, whenever we call a function from a ```ViewModel``` that begins with ```suspend```, we need to call it using ```coroutineScope.launch {}```, and we define the coroutine scope like ```val coroutineScope = rememberCoroutineScope()```.

Here is an example of how the ```ItemEntryViewModel``` is used in the ```ItemEntryScreen```:

```Kotlin
@Composable
fun ItemEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            InventoryTopAppBar(
                title = stringResource(R.string.item_entry_title),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        ItemEntryBody(
            itemUiState = viewModel.itemUiState,
            onItemValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.saveItem()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }
}
```

In conclusion, ```ViewModels``` inevitably perish under the infinitely massive weight of being a stateless object. In order to stay alive and do what they're supposed to do, ```ViewModels``` need to rely on their friends like ```ViewModelProviders```, ```ViewModelFactories```, ```CreationExtras``` instances, ```Application()``` overriding classes, Repositories, and ```AppContainers```.

Here is the whole thing in a simple flow:

### Summary of Flow when using Room

When the app starts, Android reads the manifest (```AndroidManifest.xml```). When the manifest reads:

```xml
<application
    android:name=".InventoryApplication"
    ...>
```

Android overrides the default ```Application()``` class with ```InventoryApplication()```.

Then, Android runs the ```onCreate()``` function that we overrode in ```InventoryApplication()```:

```Kotlin
override fun onCreate() {
    super.onCreate()
    container = AppDataContainer(this)
}
```

So now the app has one shared container. This ```AppDataContainer``` now knows how to create the repository since we've supplied it with the context from ```InventoryApplication()```:

```Kotlin
/**
 * AppDataContainer gets the Room database, asks the database for the DAO,
 * and finally gives that DAO to the OfflineItemsRepository.
 */
override val itemsRepository: ItemsRepository by lazy {
    OfflineItemsRepository(
        itemDao = InventoryDatabase.getDatabase(context).itemDao()
    )
}
```

Now the entire data side of the application is connected like this:

1. The ```InventoryDatabase``` creates and provides the ```ItemDao```.
2. The ```ItemDao``` has insert, update, delete, and other query functions.
3. The ```OfflineItemsRepository``` receives the ```ItemDao``` and calls its functions.

Then, Android launches the ```MainActivity```. ```MainActivity``` shows your compose UI and it eventually asks for a ```ViewModel```:

```Kotlin
viewModel(factory = AppViewModelProvider.Factory)
```

The factory then realizes that it needs the repository to create the ```ItemEntryViewModel```. It knows that it's an ```ItemEntryViewModel``` because it's an ```ItemEntryViewModel```. So the factory does this:

```Kotlin
ItemEntryViewModel(
    inventoryApplication().container.itemsRepository
)
```

Then ```inventoryApplication()``` (a function) gets the already-created ```InventoryApplication``` from ```CreationExtras```. Then it grabs the ```AppContainer``` because it is an ```InventoryApplication```, and that is what an ```InventoryApplication``` does. It then finds that the ```AppContainer``` is in fact an ```AppDataContainer```, which contains the repository, so it grabs that and passes it into the ```ViewModel```.

So then when the user taps 'Save':

1. ```ItemEntryScreen``` calls ```viewModel.saveItem()```
2. ```ItemEntryViewModel``` calls ```itemsRepository.insertItem(item)```
3. ```OfflineItemsRepository``` calls ```itemDao.insert(item)```
4. Room DAO inserts into SQL database

*OR*, you could just do ...

```Kotlin
val database = InventoryDatabase.getDatabase(context)
val dao = database.itemDao()
val repository = OfflineItemsRepository(dao)
val viewModel = ItemEntryViewModel(repository)
```

to avoid literally all of this, but it would slow the crap out of your phone and would probably result in some backend errors. Using the former strategy, the app builds these things once at startup and lets the View Model factory hand them to each ```ViewModel``` when needed.
