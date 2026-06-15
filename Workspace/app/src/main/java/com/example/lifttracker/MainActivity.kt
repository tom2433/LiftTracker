package com.example.lifttracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lifttracker.ui.SportsApp
import com.example.lifttracker.ui.theme.LiftTrackerTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {        // onCreate = main
        super.onCreate(savedInstanceState)

        // log the status with the TAG
        Log.d(TAG, "onCreate Called")

        enableEdgeToEdge()
        setContent {
            LiftTrackerTheme(dynamicColor = false) {
                // surface container using background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val windowSize = calculateWindowSizeClass(this)
                    SportsApp(windowSize = windowSize.widthSizeClass)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart Called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart Called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause Called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDesto")
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(widthDp = 411, heightDp = 923, name = "Pixel")
@Composable
fun SportsAppCompactPreviewPixel() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SportsApp(WindowWidthSizeClass.Compact)
        }
    }
}

@Preview(widthDp = 673, heightDp = 841, name = "Foldable")
@Composable
fun SportsAppCompactPreviewFoldable() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SportsApp(WindowWidthSizeClass.Medium)
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800, name = "Tablet")
@Composable
fun SportsAppCompactPreviewTablet() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SportsApp(WindowWidthSizeClass.Expanded)
        }
    }
}

