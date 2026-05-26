package com.example.lifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.font.FontWeight
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
                    // put grid stuff here
                    GridScreen()
                }
            }
        }
    }
}

@Composable
fun GridElement(heading: String, body: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color = color)
            .padding(16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // header
        Text(
            text = heading,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                bottom = 16.dp
            )
        )
        // body
        Text(
            text = body,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun GridScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.5f),
            horizontalArrangement = Arrangement.Start
        ) {
            GridElement(
                "Text Composable",
                "Displays text and follows the recommended Material Design guidelines.",
                Color(0xFFEADDFF),
                Modifier.weight(0.5f)
            )
            GridElement(
                "Image composable",
                "Creates a composable that lays out and draws a given Painter class object.",
                Color(0xFFD0BCFF),
                Modifier.weight(0.5f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(0.5f),
            horizontalArrangement = Arrangement.Start
        ) {
            GridElement(
                "Row composable",
                "A layout composable that places its children in a horizontal sequence.",
                Color(0xFFB69DF8),
                Modifier.weight(0.5f)
            )
            GridElement(
                "Column composable",
                "A layout composable that places its children in a vertical sequence.",
                Color(0xFFF6EDFF),
                Modifier.weight(0.5f)
            )
        }
    }
}

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