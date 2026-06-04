package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifttracker.model.Affirmation
import com.example.lifttracker.ui.theme.LiftTrackerTheme
import com.example.lifttracker.data.Datasource
import java.text.NumberFormat

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
                    AffirmationsApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AffirmationsAppPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AffirmationsApp()
    }
}

@Composable
fun AffirmationsApp() {
    val layoutDirection = LocalLayoutDirection.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                start = WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(layoutDirection),
                end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(layoutDirection)
            )
    ) {
        AffirmationList(
            affirmationList = Datasource().loadAffirmations()
        )
    }
}

@Composable
fun AffirmationCard(affirmation: Affirmation, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column {
            Image(
                painter = painterResource(affirmation.imageResourceID),
                contentDescription = stringResource(affirmation.stringResourceId),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = stringResource(affirmation.stringResourceId),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
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

//
//@Preview(showBackground = true)
//@Composable
//fun ItemsOnMyDeskAppPreview() {
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        ItemsOnMyDeskLayout()
//    }
//}
//
//@Composable
//fun ItemsOnMyDeskLayout(modifier: Modifier = Modifier) {
//    Column (
//        modifier = modifier
//            .statusBarsPadding()
//            .padding(horizontal = 20.dp)
//            .safeDrawingPadding()
//            .fillMaxSize(),
//        verticalArrangement = Arrangement.Bottom,
//        horizontalAlignment = Alignment.Start
//    ) {
//        var imageNum by remember { mutableIntStateOf(1) }
//
//        @DrawableRes var currentImage: Int
//        var currentTitle: String
//        var currentDesc: String
//
//        when (imageNum) {
//            1 -> {
//                currentImage = R.drawable.computer_mouse
//                currentTitle = stringResource(R.string.computer_mouse_title)
//                currentDesc = stringResource(R.string.computer_mouse_description)
//            }
//            2 -> {
//                currentImage = R.drawable.energy_drink
//                currentTitle = stringResource(R.string.energy_drink_title)
//                currentDesc = stringResource(R.string.energy_drink_description)
//            }
//            3 -> {
//                currentImage = R.drawable.pen
//                currentTitle = stringResource(R.string.pen_title)
//                currentDesc = stringResource(R.string.pen_description)
//            }
//            else -> {
//                currentImage = R.drawable.power_bank
//                currentTitle = stringResource(R.string.power_bank_title)
//                currentDesc = stringResource(R.string.power_bank_description)
//            }
//        }
//
//        ImagePane(
//            image = currentImage,
//            contentDescription = currentTitle,
//            modifier = Modifier.weight(1f)
//        )
//
//        TitleSection(
//            title = currentTitle,
//            description = currentDesc,
//            modifier = Modifier.padding(bottom = 50.dp)
//        )
//
//        ButtonRow(
//            onClickPrev = {
//                if (imageNum == 1) {
//                    imageNum = 4
//                } else {
//                    imageNum--
//                }
//            },
//            onClickNext = {
//                if (imageNum == 4) {
//                    imageNum = 1
//                } else {
//                    imageNum++
//                }
//            },
//            modifier = Modifier.padding(bottom = 12.dp)
//        )
//    }
//}
//
//@Composable
//fun ButtonRow(
//    onClickPrev: () -> Unit,
//    onClickNext: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//    ) {
//        // previous button
//        Button(
//            modifier = Modifier.weight(2f),
//            onClick = onClickPrev
//        ) {
//            Text(
//                text = "Previous"
//            )
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
//
//        // next button
//        Button(
//            modifier = Modifier.weight(2f),
//            onClick = onClickNext
//        ) {
//            Text(
//                text = "Next"
//            )
//        }
//    }
//}
//
//@Composable
//fun TitleSection(
//    title: String,
//    description: String,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier,
//        verticalArrangement = Arrangement.Top,
//        horizontalAlignment = Alignment.Start
//    ) {
//        Text(
//            text = title,
//            textAlign = TextAlign.Start,
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Light
//        )
//        Spacer(modifier = Modifier.height(5.dp))
//        Text(
//            text = description,
//            textAlign = TextAlign.Start,
//            fontSize = 16.sp,
//            fontWeight = FontWeight.SemiBold
//        )
//    }
//}
//
//@Composable
//fun ImagePane(
//    @DrawableRes image: Int,
//    contentDescription: String,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = modifier.fillMaxWidth()
//    ) {
//        Surface(
//            modifier = Modifier
//                .wrapContentWidth(),
//            shadowElevation = 8.dp
//        ) {
//            val painter = painterResource(image)
//
//            Image(
//                painter = painter,
//                contentDescription = contentDescription,
//                modifier = Modifier.padding(24.dp)
//            )
//        }
//    }
//}
