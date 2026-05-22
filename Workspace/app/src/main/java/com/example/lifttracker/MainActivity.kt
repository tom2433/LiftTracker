package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifttracker.ui.theme.LiftTrackerTheme
import org.intellij.lang.annotations.JdkConstants

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
                    // put grid stuff here
                }
            }
        }
    }
}

//@Composable
//fun AlertScreen(graphic: Painter, heading: String, subHeading: String, modifier: Modifier = Modifier) {
//    Column(
//        modifier = modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // checkmark graphic
//        Image(
//            painter = graphic,
//            contentDescription = null
//        )
//        // heading: bold, padding (24dp top, 8dp bottom)
//        Text(
//            text = heading,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(
//                top = 24.dp,
//                bottom = 8.dp
//            )
//        )
//        // sub heading: 16sp font size
//        Text(
//            text = subHeading,
//            fontSize = 16.sp
//        )
//    }
//}

@Composable
fun GridScreen(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier.weight(0.5f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun TasksCompletedPreview() {
//    LiftTrackerTheme {
//        val graphic = painterResource(R.drawable.check)
//        val heading = "All tasks completed"
//        val subHeading = "Nice Work!"
//
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            AlertScreen(graphic, heading, subHeading)
//        }
//    }
//}

@Preview(showBackground = true)
@Composable
fun GridPreview() {
    LiftTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // show grid screen here
            GridScreen()
        }
    }
}