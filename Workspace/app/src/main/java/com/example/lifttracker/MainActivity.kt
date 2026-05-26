package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    // put business card stuff here
                    BusinessCardScreen()
                }
            }
        }
    }
}

//@Composable
//fun GridElement(heading: String, body: String, color: Color, modifier: Modifier = Modifier) {
//    Column(
//        modifier = modifier
//            .background(color = color)
//            .padding(16.dp)
//            .fillMaxHeight(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // header
//        Text(
//            text = heading,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(
//                bottom = 16.dp
//            )
//        )
//        // body
//        Text(
//            text = body,
//            textAlign = TextAlign.Justify
//        )
//    }
//}
//
//@Composable
//fun GridScreen(modifier: Modifier = Modifier) {
//    Column(
//        modifier = modifier.fillMaxSize()
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth().weight(0.5f),
//            horizontalArrangement = Arrangement.Start
//        ) {
//            GridElement(
//                "Text Composable",
//                "Displays text and follows the recommended Material Design guidelines.",
//                Color(0xFFEADDFF),
//                Modifier.weight(0.5f)
//            )
//            GridElement(
//                "Image composable",
//                "Creates a composable that lays out and draws a given Painter class object.",
//                Color(0xFFD0BCFF),
//                Modifier.weight(0.5f)
//            )
//        }
//        Row(
//            modifier = Modifier.fillMaxWidth().weight(0.5f),
//            horizontalArrangement = Arrangement.Start
//        ) {
//            GridElement(
//                "Row composable",
//                "A layout composable that places its children in a horizontal sequence.",
//                Color(0xFFB69DF8),
//                Modifier.weight(0.5f)
//            )
//            GridElement(
//                "Column composable",
//                "A layout composable that places its children in a vertical sequence.",
//                Color(0xFFF6EDFF),
//                Modifier.weight(0.5f)
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GridPreview() {
//    LiftTrackerTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            // show grid screen here
//            GridScreen()
//        }
//    }
//}

@Composable
fun NameCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        border = BorderStroke   (2.dp, Color.Red)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // image surface (weight = 4)
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .weight(4f),
                border = BorderStroke(2.dp, Color.Blue),
                color = Color(7, 48, 66)
            ) {
                // Image
                val image = painterResource(R.drawable.android_logo)

                Image(
                    painter = image,
                    contentDescription = "Android Logo"
                )
            }

            // name surface (weight = 1.5)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
                border = BorderStroke(2.dp, Color.Blue)
            ) {
                // full name
            }

            // title surface (weight = 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                border = BorderStroke(2.dp, Color.Blue)
            ) {
                // title
            }
        }
    }
}

@Composable
fun ContactCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(2.dp, Color.Red)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // phone surface (weight = 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                border = BorderStroke(2.dp, Color.Blue)
            ) {
                // phone number row
            }

            // social media surface (weight = 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                border = BorderStroke(2.dp, Color.Blue)
            ) {
                // social media handle row
            }

            // email surface (weight = 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                border = BorderStroke(2.dp, Color.Blue)
            ) {
                // email address row
            }
        }
    }
}

@Composable
fun BusinessCardScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        NameCard(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.weight(0.5f))
        ContactCard(modifier = Modifier.weight(0.8f))
        Spacer(modifier = Modifier.weight(0.2f))
    }
}

@Preview(showBackground = true)
@Composable
fun BusinessCardPreview() {
    LiftTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // show BusinessCard screen here
            BusinessCardScreen()
        }
    }
}