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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun NameCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
//        border = BorderStroke(2.dp, Color.Red)
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
                4.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // image surface (weight = 3)
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .weight(3.5f),
//                border = BorderStroke(2.dp, Color.Blue),
                color = Color(7, 48, 66)
            ) {
                Surface(
                    modifier = Modifier.padding(6.dp),
                    color = Color(7, 48, 66)
                ) {
                    // Image
                    val image = painterResource(R.drawable.android_logo)

                    Image(
                        painter = image,
                        contentDescription = "Android Logo"
                    )
                }
            }

            // name surface (weight = 1.5)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
//                border = BorderStroke(2.dp, Color.Blue)
            ) {
                // full name
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Thomas England",
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // title surface (weight = 1)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
//                border = BorderStroke(2.dp, Color.Blue)
            ) {
                // title
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Android Developer",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color(4, 115, 65)
                    )
                }
            }
        }
    }
}

@Composable
fun IconRow(icon: ImageVector, iconDescription: String, content: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
//        border = BorderStroke(2.dp, Color.Blue)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(
                14.dp,
                alignment = Alignment.Start
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // start spacer
            Spacer(modifier = Modifier.weight(1f))

            // icon surface
            Surface(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxSize(),
//                border = BorderStroke(2.dp, Color.Green)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = Color(4, 115, 65)
                )
            }

            // text surface
            Surface(
                modifier = Modifier
                    .weight(6f)
                    .fillMaxWidth(),
//                border = BorderStroke(2.dp, Color.Green)
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = content,
                        textAlign = TextAlign.Left,
                        fontSize = 13.sp
                    )
                }
            }

            // end spacer
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ContactCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
//        border = BorderStroke(2.dp, Color.Red)
    ) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(
                4.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // phone number row
            IconRow(
                Icons.Rounded.Phone,
                "Phone",
                "+1 (630) 967 4510",
                Modifier.weight(1f)
            )

            // social media handle row
            IconRow(
                Icons.Rounded.AccountCircle,
                "Account Circle",
                "linkedin.com/in/thomasjengland",
                Modifier.weight(1f)
            )

            // email address row
            IconRow(
                Icons.Rounded.Mail,
                "Mail",
                "thomas.j.england@gmail.com",
                Modifier.weight(1f)
            )
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
        Spacer(modifier = Modifier.weight(0.9f))            // previously 0.9f
        NameCard(modifier = Modifier.weight(0.8f))          // previously 0.9f
        Spacer(modifier = Modifier.weight(0.6f))            // previously 0.6f
        ContactCard(modifier = Modifier.weight(0.5f))       // previously 0.7f
        Spacer(modifier = Modifier.weight(0.5f))            // previously 0.3f
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