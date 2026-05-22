package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifttracker.ui.theme.LiftTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {        // onCreate = main
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme {
//                // surface container using background color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    GreetingImage(
//                        message = "Hello, World!",
//                        from = "Bottom text"
//                    )
//                }

                val image = painterResource(R.drawable.banner)
                val title = stringResource(R.string.jetpack_compose_tutorial_title)
                val intro = stringResource(R.string.jetpack_compose_tutorial_intro)
                val body = stringResource(R.string.jetpack_compose_tutorial_body)

                // surface container using background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // add ArticleScreen here
                    ArticleScreen(image, title, intro, body)
                }
            }
        }
    }
}

//@Composable
//fun GreetingText(message: String, from: String, modifier: Modifier = Modifier) {
//    Column(
//        verticalArrangement = Arrangement.Center,
//        modifier = modifier
//    ) {
//        Text(
//            text = message,
////            modifier = modifier.padding(24.dp)
//            fontSize = 100.sp,
//            lineHeight = 116.sp,
//            textAlign = TextAlign.Center
//        )
//        Text(
//            text = from,
//            fontSize = 36.sp,
//            modifier = Modifier
//                .padding(16.dp)
//                .align(alignment = Alignment.CenterHorizontally)
//        )
//    }
//}
//
//@Composable
//fun GreetingImage(message: String, from: String, modifier: Modifier = Modifier) {
//    val image = painterResource(R.drawable.background)
//    Box(modifier = modifier.fillMaxSize()) {
//        Image(
//            painter = image,
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop,
//            alpha = 0.5F
//        )
//        GreetingText(
//            message = message,
//            from = from,
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(8.dp)
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    LiftTrackerTheme {
//        GreetingImage(stringResource(R.string.hello_world), stringResource(R.string.bottom_text))
//    }
//}

@Composable
fun ArticleScreen(bannerImg: Painter, title: String, intro: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = bannerImg,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            // 24sp font size, 16dp padding (all)
            text = title,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            // default font size, 16dp padding (start and end), and justify text align
            text = intro,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp
            )
        )
        Text(
            // default font size, 16dp padding (all), justify text align
            text = body,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArticlePreview() {
    LiftTrackerTheme {
        val image = painterResource(R.drawable.banner)
        val title = stringResource(R.string.jetpack_compose_tutorial_title)
        val intro = stringResource(R.string.jetpack_compose_tutorial_intro)
        val body = stringResource(R.string.jetpack_compose_tutorial_body)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ArticleScreen(image, title, intro, body)
        }
    }
}