package com.example.lifttracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import com.example.lifttracker.ui.ReplyApp
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
                val layoutDirection = LocalLayoutDirection.current

                // surface container using background color from the theme
                Surface(
                    modifier = Modifier
                        .padding(
                            start = WindowInsets.safeDrawing.asPaddingValues()
                                .calculateStartPadding(layoutDirection),
                            end = WindowInsets.safeDrawing.asPaddingValues()
                                .calculateEndPadding(layoutDirection)
                        ),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val windowSize = calculateWindowSizeClass(this)
                    ReplyApp(windowSize = windowSize.widthSizeClass)
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
fun ReplyAppCompactPreviewPixel() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ReplyApp(WindowWidthSizeClass.Compact)
        }
    }
}

@Preview(widthDp = 673, heightDp = 841, name = "Foldable")
@Composable
fun ReplyAppCompactPreviewFoldable() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ReplyApp(WindowWidthSizeClass.Medium)
        }
    }
}

@Preview(widthDp = 1280, heightDp = 800, name = "Tablet")
@Composable
fun ReplyAppCompactPreviewTablet() {
    LiftTrackerTheme(dynamicColor = false, darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ReplyApp(WindowWidthSizeClass.Expanded)
        }
    }
}

