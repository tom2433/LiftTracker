package com.example.lifttracker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.lifttracker.data.Datasource
import com.example.lifttracker.model.Dessert
import com.example.lifttracker.ui.theme.LiftTrackerTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {        // onCreate = main
        super.onCreate(savedInstanceState)

        // log the status with the TAG
        Log.d(TAG, "onCreate Called")

        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme(dynamicColor = false) {
                // surface container using background color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    DessertClickerApp(desserts = Datasource.dessertList)
                }
            }
        }
    }

    override fun onStart() {
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

/**
 * Determine which dessert to show.
 */
fun determineDessertToShow(
    desserts: List<Dessert>,
    dessertsSold: Int
): Dessert {
    var dessertToShow = desserts.first()
    for (dessert in desserts) {
        if (dessertsSold >= dessert.startProductionAmount) {
            dessertToShow = dessert
        } else {
            // The list of desserts is sorted by startProductionAmount. As you sell more desserts,
            // you'll start producing more expensive desserts as determined by startProductionAmount
            // We know to break as soon as we see a dessert who's "startProductionAmount" is greater
            // than the amount sold.
            break
        }
    }

    return dessertToShow
}

/**
 * Share desserts sold information using ACTION_SEND intent
 */
private fun shareSoldDessertsInformation(intentContext: Context, dessertsSold: Int, revenue: Int) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            intentContext.getString(R.string.share_text, dessertsSold, revenue)
        )
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)

    try {
        @Suppress("DEPRECATION")
        ContextCompat.startActivity(intentContext, shareIntent, null)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            intentContext,
            intentContext.getString(R.string.sharing_not_available),
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
private fun DessertClickerApp(
    desserts: List<Dessert>
) {

    var revenue by rememberSaveable { mutableIntStateOf(0) }
    var dessertsSold by rememberSaveable { mutableIntStateOf(0) }

    val currentDessertIndex by rememberSaveable { mutableIntStateOf(0) }

    var currentDessertPrice by rememberSaveable {
        mutableIntStateOf(desserts[currentDessertIndex].price)
    }
    var currentDessertImageId by rememberSaveable {
        mutableIntStateOf(desserts[currentDessertIndex].imageId)
    }

    Scaffold(
        topBar = {
            val intentContext = LocalContext.current
            val layoutDirection = LocalLayoutDirection.current
            DessertClickerAppBar(
                onShareButtonClicked = {
                    shareSoldDessertsInformation(
                        intentContext = intentContext,
                        dessertsSold = dessertsSold,
                        revenue = revenue
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateStartPadding(layoutDirection),
                        end = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateEndPadding(layoutDirection),
                    )
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) { contentPadding ->
        DessertClickerScreen(
            revenue = revenue,
            dessertsSold = dessertsSold,
            dessertImageId = currentDessertImageId,
            onDessertClicked = {

                // Update the revenue
                revenue += currentDessertPrice
                dessertsSold++

                // Show the next dessert
                val dessertToShow = determineDessertToShow(desserts, dessertsSold)
                currentDessertImageId = dessertToShow.imageId
                currentDessertPrice = dessertToShow.price
            },
            modifier = Modifier.padding(contentPadding)
        )
    }
}

@Composable
private fun DessertClickerAppBar(
    onShareButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(
            onClick = onShareButtonClicked,
            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium)),
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun DessertClickerScreen(
    revenue: Int,
    dessertsSold: Int,
    @DrawableRes dessertImageId: Int,
    onDessertClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bakery_back),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(dessertImageId),
                    contentDescription = null,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.image_size))
                        .height(dimensionResource(R.dimen.image_size))
                        .align(Alignment.Center)
                        .clickable { onDessertClicked() },
                    contentScale = ContentScale.Crop,
                )
            }
            TransactionInfo(
                revenue = revenue,
                dessertsSold = dessertsSold,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

@Composable
private fun TransactionInfo(
    revenue: Int,
    dessertsSold: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DessertsSoldInfo(
            dessertsSold = dessertsSold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
        RevenueInfo(
            revenue = revenue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
    }
}

@Composable
private fun RevenueInfo(revenue: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.total_revenue),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$${revenue}",
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun DessertsSoldInfo(dessertsSold: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.dessert_sold),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = dessertsSold.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Preview
@Composable
fun MyDessertClickerAppPreview() {
    LiftTrackerTheme {
        DessertClickerApp(listOf(Dessert(R.drawable.cupcake, 5, 0)))
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun TopicsAppPreview() {
//    LiftTrackerTheme(
//        dynamicColor = false,
//        darkTheme = true
//    ) {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            TopicsApp()
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TopicTopAppBar(modifier: Modifier = Modifier) {
//    CenterAlignedTopAppBar(
//        title = {
//            Row {
//                Image(
//                    modifier = Modifier
//                        .width(64.dp)
//                        .height(64.dp)
//                        .padding(8.dp),
//                    painter = painterResource(R.drawable.walmart),
//                    contentDescription = null
//                )
//                Text(
//                    text = "Topics App",
//                    style = MaterialTheme.typography.displayLarge,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 24.sp
//                )
//            }
//        },
//        modifier = modifier
//    )
//}
//
//@Composable
//fun TopicsApp() {
//    val topicCardList = Datasource().loadTopics()
//
//    // pair each element with index and split based on index
//    val (evensWithIndex, oddsWithIndex) = topicCardList.withIndex().partition { it.index % 2 == 0 }
//
//    // remove indices to be left with 2 separate lists
//    val leftColumnCards = evensWithIndex.map { it.value }
//    val rightColumnCards = oddsWithIndex.map { it.value }
//
//    // Scaffold to hold TopAppBar and Contents
//    Scaffold(
//        topBar = {
//            TopicTopAppBar()
//        },
//        modifier = Modifier
//            .fillMaxSize()
//            .statusBarsPadding()
//    ) { innerPadding ->
//        // column with scroll bars (holds everything)
//        Column(
//            modifier = Modifier
//                .verticalScroll(rememberScrollState())
//                .padding(innerPadding)
//        ) {
//            // row to hold two columns of cards
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(8.dp)
//            ) {
//                // left column of cards
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    leftColumnCards.forEach { topic ->
//                        TopicCard(
//                            topic = topic,
//                            modifier = Modifier.padding(
//                                bottom = 8.dp,
//                                end = 8.dp
//                            )
//                        )
//                    }
//                }
//                // right column of cards
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    rightColumnCards.forEach { topic ->
//                        TopicCard(
//                            topic = topic,
//                            modifier = Modifier.padding(
//                                bottom = 8.dp
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TopicCard(
//    topic: Topic,
//    modifier: Modifier = Modifier
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val color by animateColorAsState(
//        targetValue = if (expanded) {
//            MaterialTheme.colorScheme.primaryContainer
//        } else {
//            CardDefaults.cardColors().containerColor
//        }
//    )
//    val textColor by animateColorAsState(
//        targetValue = if (expanded) {
//            MaterialTheme.colorScheme.onPrimaryContainer
//        } else {
//            MaterialTheme.colorScheme.onBackground
//        }
//    )
//
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .wrapContentHeight(),
//        shape = RoundedCornerShape(
//            bottomStart = 16.dp,
//            topEnd = 16.dp
//        ),
//        onClick = { expanded = !expanded }
//    ) {
//        Column(
//            modifier = Modifier
//                .animateContentSize(
//                    animationSpec = spring(
//                        dampingRatio = Spring.DampingRatioLowBouncy,
//                        stiffness = Spring.StiffnessMediumLow
//                    )
//                )
//                .background(color = color)
//        ) {
//            // row holding image, text
//            Row(
//                horizontalArrangement = Arrangement.Start,
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                // image for topic
//                Image(
//                    painter = painterResource(topic.imageResourceId),
//                    contentDescription = stringResource(topic.stringResourceId),
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .width(60.dp)
//                        .height(60.dp)
//                        .padding(8.dp)
//                        .clip(CircleShape)
//                )
//
//                // column holding name of topic and number
//                Column(
//                    verticalArrangement = Arrangement.Top,
//                    horizontalAlignment = Alignment.Start,
//                    modifier = Modifier
//                        .padding(
//                            start = 12.dp
//                        )
//                        .wrapContentSize()
//                ) {
//                    // name of topic
//                    Text(
//                        text = stringResource(topic.stringResourceId),
//                        textAlign = TextAlign.Left,
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontSize = 12.sp,
//                        color = textColor,
//                        modifier = Modifier.padding(bottom = 4.dp)
//                    )
//
//                    // row holding icon and number
//                    Row(
//                        horizontalArrangement = Arrangement.Start,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Rounded.Assessment,
//                            contentDescription = "Assessment",
//                            tint = textColor,
//                            modifier = Modifier.padding(end = 8.dp)
//                        )
//                        Text(
//                            text = topic.statusNumber.toString(),
//                            textAlign = TextAlign.Left,
//                            style = MaterialTheme.typography.labelMedium,
//                            color = textColor
//                        )
//                    }
//                }
//
//                // spacer to separate expand more icon
//                Spacer(modifier = Modifier.weight(1f))
//
//                Icon(
//                    imageVector = if (expanded) {
//                        Icons.Rounded.ExpandLess
//                    } else {
//                        Icons.Rounded.ExpandMore
//                    },
//                    contentDescription = if (expanded) {
//                        "Expand Less"
//                    } else {
//                        "Expand More"
//                    },
//                    tint = textColor,
//                    modifier = Modifier
//                        .padding(end = 4.dp)
//                        .height(20.dp)
//                )
//            }
//
//            // bottom text for when the user clicks the card
//            if (expanded) {
//                Text(
//                    text = "Bottom text.",
//                    modifier = Modifier.padding(
//                        start = 8.dp,
//                        bottom = 8.dp
//                    ),
//                    color = textColor
//                )
//            }
//        }
//    }
//}
