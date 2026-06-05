package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifttracker.data.Datasource
import com.example.lifttracker.model.Topic
import com.example.lifttracker.ui.theme.LiftTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {        // onCreate = main
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme(dynamicColor = false) {
                // surface container using background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TopicsApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopicsAppPreview() {
    LiftTrackerTheme(
        dynamicColor = false,
        darkTheme = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TopicsApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row {
                Image(
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                        .padding(8.dp),
                    painter = painterResource(R.drawable.walmart),
                    contentDescription = null
                )
                Text(
                    text = "Topics App",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun TopicsApp() {
    val topicCardList = Datasource().loadTopics()

    // pair each element with index and split based on index
    val (evensWithIndex, oddsWithIndex) = topicCardList.withIndex().partition { it.index % 2 == 0 }

    // remove indices to be left with 2 separate lists
    val leftColumnCards = evensWithIndex.map { it.value }
    val rightColumnCards = oddsWithIndex.map { it.value }

    // Scaffold to hold TopAppBar and Contents
    Scaffold(
        topBar = {
            TopicTopAppBar()
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        // column with scroll bars (holds everything)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // row to hold two columns of cards
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // left column of cards
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    leftColumnCards.forEach { topic ->
                        TopicCard(
                            topic = topic,
                            modifier = Modifier.padding(
                                bottom = 8.dp,
                                end = 8.dp
                            )
                        )
                    }
                }
                // right column of cards
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    rightColumnCards.forEach { topic ->
                        TopicCard(
                            topic = topic,
                            modifier = Modifier.padding(
                                bottom = 8.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopicCard(
    topic: Topic,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val color by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            CardDefaults.cardColors().containerColor
        }
    )
    val textColor by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onBackground
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(
            bottomStart = 16.dp,
            topEnd = 16.dp
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .background(color = color)
        ) {
            // row holding image, text
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // image for topic
                Image(
                    painter = painterResource(topic.imageResourceId),
                    contentDescription = stringResource(topic.stringResourceId),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                        .padding(8.dp)
                        .clip(CircleShape)
                )

                // column holding name of topic and number
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .padding(
                            start = 12.dp
                        )
                        .wrapContentSize()
                ) {
                    // name of topic
                    Text(
                        text = stringResource(topic.stringResourceId),
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // row holding icon and number
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Assessment,
                            contentDescription = "Assessment",
                            tint = textColor,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = topic.statusNumber.toString(),
                            textAlign = TextAlign.Left,
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor
                        )
                    }
                }

                // spacer to separate expand more icon
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = if (expanded) {
                        Icons.Rounded.ExpandLess
                    } else {
                        Icons.Rounded.ExpandMore
                    },
                    contentDescription = if (expanded) {
                        "Expand Less"
                    } else {
                        "Expand More"
                    },
                    tint = textColor,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .height(20.dp)
                )
            }

            // bottom text for when the user clicks the card
            if (expanded) {
                Text(
                    text = "Bottom text.",
                    modifier = Modifier.padding(
                        start = 8.dp,
                        bottom = 8.dp
                    ),
                    color = textColor
                )
            }
        }
    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun AffirmationsAppPreview() {
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        AffirmationsApp()
//    }
//}
//
//@Composable
//fun AffirmationsApp() {
//    val layoutDirection = LocalLayoutDirection.current
//    Surface(
//        modifier = Modifier
//            .fillMaxSize()
//            .statusBarsPadding()
//            .padding(
//                start = WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(layoutDirection),
//                end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(layoutDirection)
//            )
//    ) {
//        AffirmationList(
//            affirmationList = Datasource().loadAffirmations()
//        )
//    }
//}
//
//@Composable
//fun AffirmationCard(affirmation: Affirmation, modifier: Modifier = Modifier) {
//    Card(modifier = modifier) {
//        Column {
//            Image(
//                painter = painterResource(affirmation.imageResourceID),
//                contentDescription = stringResource(affirmation.stringResourceId),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(194.dp),
//                contentScale = ContentScale.Crop
//            )
//            Text(
//                text = stringResource(affirmation.stringResourceId),
//                modifier = Modifier.padding(16.dp),
//                style = MaterialTheme.typography.headlineSmall
//            )
//        }
//    }
//}
//
//@Composable
//fun AffirmationList(affirmationList: List<Affirmation>, modifier: Modifier = Modifier) {
//    LazyColumn(modifier = modifier) {
//        items(affirmationList) { affirmation ->
//            AffirmationCard(
//                affirmation = affirmation,
//                modifier = Modifier.padding(8.dp)
//            )
//        }
//    }
//}
