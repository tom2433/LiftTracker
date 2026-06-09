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
    var color by animateColorAsState(
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
