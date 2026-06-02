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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifttracker.ui.theme.LiftTrackerTheme
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
                    ItemsOnMyDeskLayout()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemsOnMyDeskAppPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ItemsOnMyDeskLayout()
    }
}

@Composable
fun ItemsOnMyDeskLayout(modifier: Modifier = Modifier) {
    Column (
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .safeDrawingPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start
    ) {
        ImagePane(
            image = R.drawable.computer_mouse,
            modifier = Modifier.weight(1f)
        )

        TitleSection(
            title = "My Computer Mouse",
            description = "This mouse is very expensive.",
            modifier = Modifier.padding(bottom = 50.dp)
        )

        ButtonRow(modifier = Modifier.padding(bottom = 12.dp))
    }
}

@Composable
fun ButtonRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        // previous button
        Button(
            modifier = Modifier.weight(2f),
            onClick = { /*TODO*/ },
        ) {
            Text(
                text = "Previous"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // next button
        Button(
            modifier = Modifier.weight(2f),
            onClick = { /*TODO*/ }
        ) {
            Text(
                text = "Next"
            )
        }
    }
}

@Composable
fun TitleSection(title: String, description: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            fontSize = 32.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = description,
            textAlign = TextAlign.Start,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ImagePane(
    modifier: Modifier = Modifier,
    @DrawableRes image: Int
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            val painter = painterResource(image)

            Image(
                painter = painter,
                contentDescription = "computer mouse",
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

//
//@Composable
//fun EditNumberField(
//    @StringRes label: Int,
//    leadingIcon: ImageVector,
//    keyboardOptions: KeyboardOptions,
//    value: String,
//    onValueChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    TextField(
//        value = value,
//        leadingIcon = {
//            Icon(
//                imageVector = leadingIcon,
//                contentDescription = null
//            )
//        },
//        onValueChange = onValueChange,
//        label = {
//            Text(
//                text = stringResource(label)
//            )
//        },
//        singleLine = true,
//        modifier = modifier,
//        keyboardOptions = keyboardOptions
//    )
//}
//
//@Composable
//fun TipTimeLayout() {
//    var amountInput by remember { mutableStateOf("") }
//    var tipInput by remember { mutableStateOf("") }
//    var roundUp by remember { mutableStateOf(false) }
//
//    val amount = amountInput.toDoubleOrNull() ?: 0.0
//    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0
//    val tip = calculateTip(amount, tipPercent, roundUp)
//    val total = amount + tip
//    val tipString = NumberFormat.getCurrencyInstance().format(tip)
//    val totalString = NumberFormat.getCurrencyInstance().format(total)
//
//    Column(
//        modifier = Modifier
//            .statusBarsPadding()
//            .padding(horizontal = 40.dp)
//            .safeDrawingPadding()
//            .verticalScroll(rememberScrollState()),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = stringResource(R.string.calculate_tip),
//            modifier = Modifier
//                .padding(
//                    bottom = 16.dp,
//                    top = 40.dp
//                )
//                .align(alignment = Alignment.Start)
//        )
//
//        // Bill Amount
//        EditNumberField(
//            label = R.string.bill_amount,
//            leadingIcon = Icons.Rounded.AttachMoney,
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//                imeAction = ImeAction.Next
//            ),
//            value = amountInput,
//            onValueChange = {
//                amountInput = it
//            },
//            modifier = Modifier
//                .padding(bottom = 32.dp)
//                .fillMaxWidth()
//        )
//
//        // Tip Percentage
//        EditNumberField(
//            label = R.string.how_was_the_service,
//            leadingIcon = Icons.Rounded.Percent,
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//                imeAction = ImeAction.Done
//            ),
//            value = tipInput,
//            onValueChange = {
//                tipInput = it
//            },
//            modifier = Modifier
//                .padding(bottom = 32.dp)
//                .fillMaxWidth()
//        )
//
//        // prompt user if they want to round up the tip
//        RoundTheTipRow(
//            roundUp = roundUp,
//            onRoundUpChanged = { roundUp = it },
//            modifier = Modifier.padding(bottom = 32.dp)
//        )
//
//        // tip amount
//        Text(
//            text = stringResource(R.string.tip_amount, tipString),
//            style = MaterialTheme.typography.displaySmall,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        // total + tip amount
//        Text(
//            text = stringResource(R.string.total_amount, totalString),
//            style = MaterialTheme.typography.displaySmall
//        )
//
//        Spacer(modifier = Modifier.height(150.dp))
//    }
//}
//
//@Composable
//fun RoundTheTipRow(
//    roundUp: Boolean,
//    onRoundUpChanged: (Boolean) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .size(48.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = stringResource(R.string.round_up_tip)
//        )
//        Switch(
//            checked = roundUp,
//            onCheckedChange = onRoundUpChanged,
//            modifier = modifier
//                .fillMaxWidth()
//                .wrapContentWidth(Alignment.End)
//        )
//    }
//}
//
///**
// * Calculates the tip based on the user input and format the tip amount according to the local
// * currency.
// * Example would be "$10.00".
// */
//private fun calculateTip(
//    amount: Double,
//    tipPercent: Double = 20.0,
//    roundUp: Boolean
//): Double {
//    var tip = tipPercent / 100 * amount
//
//    if (roundUp) {
//        tip = kotlin.math.ceil(tip)
//    }
//
//    return tip
//}
//
//@Preview(showBackground = true)
//@Composable
//fun TipTimePreview() {
//    LiftTrackerTheme {
//        TipTimeLayout()
//    }
//}
