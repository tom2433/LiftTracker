package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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
                    LemonadeApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LemonadeApp() {
    LemonadeScreen()
}

@Composable
fun LemonadeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lemonade header surface
        LemonadeHeader()

        // Lemonade Activity Container (contains image and text)
        LemonadeActivityContainer(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    }
}

@Composable
fun LemonadeActivityContainer(modifier: Modifier = Modifier) {
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var step by remember { mutableIntStateOf(1) }

        // determine image and text from step
        var imageResource: Int
        var instruction: String
        if (step == 1) {
            imageResource = R.drawable.lemon_tree
            instruction = "Tap the lemon tree to select a lemon"
        } else if (step == 2) {
            imageResource = R.drawable.lemon_squeeze
            instruction = "Keep tapping the lemon to squeeze it"
        } else if (step == 3) {
            imageResource = R.drawable.lemon_drink
            instruction = "Tap the lemonade to drink it"
        } else {
            imageResource = R.drawable.lemon_restart
            instruction = "Tap the empty glass to start again"
        }

        Button(
            onClick = {
                if (step == 4) {
                    step = 1
                } else {
                    step++
                }
            },
            shape = RoundedCornerShape(40.dp)
        ) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = "not implemented yet"
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = instruction,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LemonadeHeader(modifier: Modifier = Modifier) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color(249, 228, 75)
    ) {
        // Box to align header text
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = BottomCenter
        ) {
            Text(
                text = stringResource(R.string.lemonade),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color(0, 0, 0)
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DiceRollerApp() {
//    DiceWithButtonAndImage(modifier = Modifier
//        .fillMaxSize()
//        .wrapContentSize(Alignment.Center)
//    )
//}
//
//@Composable
//fun DiceWithButtonAndImage(modifier: Modifier = Modifier) {
//    var result by remember { mutableIntStateOf(1) }
//    val imageResource = when(result) {
//        1 -> R.drawable.dice_1
//        2 -> R.drawable.dice_2
//        3 -> R.drawable.dice_3
//        4 -> R.drawable.dice_4
//        5 -> R.drawable.dice_5
//        else -> R.drawable.dice_6
//    }
//
//    Column(
//        modifier = modifier,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Image(
//            painter = painterResource(imageResource),
//            contentDescription = result.toString()
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = { result = (1..6).random() }) {
//            Text(
//                text = stringResource(R.string.roll),
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Light
//            )
//        }
//    }
//}
