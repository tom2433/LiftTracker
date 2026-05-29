package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    TipTimeLayout()
                }
            }
        }
    }
}

@Composable
fun EditNumberField(
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(label)
            )
        },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun TipTimeLayout() {
    var amountInput by remember { mutableStateOf("") }
    var tipInput by remember { mutableStateOf("") }
    var roundUp by remember { mutableStateOf(false) }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0
    val tip = calculateTip(amount, tipPercent, roundUp)

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(
                    bottom = 16.dp,
                    top = 40.dp
                )
                .align(alignment = Alignment.Start)
        )

        // Bill Amount
        EditNumberField(
            label = R.string.bill_amount,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            value = amountInput,
            onValueChange = {
                amountInput = it
            },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )

        // Tip Percentage
        EditNumberField(
            label = R.string.how_was_the_service,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            value = tipInput,
            onValueChange = {
                tipInput = it
            },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )

        // prompt user if they want to round up the tip
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = { roundUp = it },
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = stringResource(R.string.tip_amount, tip),
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .size(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.round_up_tip)
        )
        Switch(
            checked = roundUp,
            onCheckedChange = onRoundUpChanged,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
        )
    }
}

/**
 * Calculates the tip based on the user input and format the tip amount according to the local
 * currency.
 * Example would be "$10.00".
 */
private fun calculateTip(
    amount: Double,
    tipPercent: Double = 20.0,
    roundUp: Boolean
): String {
    var tip = tipPercent / 100 * amount

    if (roundUp) {
        tip = kotlin.math.ceil(tip)
    }

    return NumberFormat.getCurrencyInstance().format(tip)
}

@Preview(showBackground = true)
@Composable
fun TipTimePreview() {
    LiftTrackerTheme {
        TipTimeLayout()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun LemonadeApp() {
//    LemonadeScreen()
//}
//
//@Composable
//fun LemonadeScreen(modifier: Modifier = Modifier) {
//    Column(
//        modifier = modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Top,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Lemonade header surface
//        LemonadeHeader()
//
//        // Lemonade Activity Container (contains image and text)
//        LemonadeActivityContainer(
//            modifier = Modifier
//                .fillMaxSize()
//                .wrapContentSize(Alignment.Center)
//        )
//    }
//}
//
//@Composable
//fun LemonadeActivityContainer(modifier: Modifier = Modifier) {
//    Column (
//        modifier = modifier,
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        var step by remember { mutableIntStateOf(1) }
//        var clickCount by remember { mutableIntStateOf((2..4).random()) }
//        var currentClick by remember { mutableIntStateOf(0) }
//
//        // determine image and text from step
//        var imageResource: Int
//        var imageResourceDescription: String
//        var instruction: String
//        when (step) {
//            1 -> {
//                imageResource = R.drawable.lemon_tree
//                instruction = stringResource(R.string.lemonade_instruction_1)
//                imageResourceDescription =
//                    stringResource(R.string.lemonade_image_content_description_1)
//            }
//            2 -> {
//                imageResource = R.drawable.lemon_squeeze
//                instruction = stringResource(R.string.lemonade_instruction_2)
//                imageResourceDescription =
//                    stringResource(R.string.lemonade_image_content_description_2)
//            }
//            3 -> {
//                imageResource = R.drawable.lemon_drink
//                instruction = stringResource(R.string.lemonade_instruction_3)
//                imageResourceDescription =
//                    stringResource(R.string.lemonade_image_content_description_3)
//            }
//            else -> {
//                imageResource = R.drawable.lemon_restart
//                instruction = stringResource(R.string.lemonade_instruction_4)
//                imageResourceDescription =
//                    stringResource(R.string.lemonade_image_content_description_4)
//            }
//        }
//
//        Button(
//            onClick = {
//                if (step == 4) {
//                    step = 1
//                } else if (step == 2) {
//                    currentClick++
//                    if (currentClick == clickCount) {
//                        step++
//                        currentClick = 0
//                        clickCount = (2..4).random()
//                    }
//                } else {
//                    step++
//                }
//            },
//            shape = RoundedCornerShape(40.dp)
//        ) {
//            Image(
//                painter = painterResource(imageResource),
//                contentDescription = imageResourceDescription
//            )
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = instruction,
//            textAlign = TextAlign.Center
//        )
//    }
//}
//
//@Composable
//fun LemonadeHeader(modifier: Modifier = Modifier) {
//    Surface(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(100.dp),
//        color = Color(249, 228, 75)
//    ) {
//        // Box to align header text
//        Box(
//            modifier = Modifier.padding(12.dp),
//            contentAlignment = BottomCenter
//        ) {
//            Text(
//                text = stringResource(R.string.lemonade),
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 20.sp,
//                color = Color(0, 0, 0)
//            )
//        }
//    }
//}
