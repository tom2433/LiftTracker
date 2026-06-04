package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lifttracker.data.Datasource
import com.example.lifttracker.model.Topic
import com.example.lifttracker.ui.theme.LiftTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {        // onCreate = main
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme {
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TopicsApp()
    }
}

@Composable
fun TopicsApp() {
    val topicCardList = Datasource().loadTopics()

    // pair each element with index and split based on index
    val (evensWithIndex, oddsWithIndex) = topicCardList.withIndex().partition { it.index % 2 == 0 }

    // remove indices to be left with 2 separate lists
    val leftColumnCards = evensWithIndex.map { it.value }
    val rightColumnCards = oddsWithIndex.map { it.value }

    // column with scroll bars (holds everything)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
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

@Composable
fun TopicCard(topic: Topic, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(topic.imageResourceId),
                contentDescription = stringResource(topic.stringResourceId),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(68.dp)
                    .height(68.dp)
            )

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp
                    )
                    .wrapContentSize()
            ) {
                Text(
                    text = stringResource(topic.stringResourceId),
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row (
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Assessment,
                        contentDescription = "Assessment",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = topic.statusNumber.toString(),
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
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
