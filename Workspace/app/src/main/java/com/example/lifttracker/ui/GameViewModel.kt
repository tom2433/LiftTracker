package com.example.lifttracker.ui

//import com.example.lifttracker.
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.lifttracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Game UI state
@Suppress("ObjectPropertyName")
private val _uiState = MutableStateFlow(GameUiState())

class GameViewModel : ViewModel() {
    // accessible Game UI state
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    /**
     * Share desserts sold information using ACTION_SEND intent
     */
    fun shareSoldDessertsInformation(intentContext: Context) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                intentContext.getString(
                    R.string.share_text,
                    _uiState.value.currentDessertsSold,
                    _uiState.value.currentRevenue)
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        try {
            @Suppress("DEPRECATION")
            ContextCompat.startActivity(intentContext, shareIntent, null)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                intentContext,
                intentContext.getString(R.string.sharing_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun onDessertClicked() {
        val updatedRevenue = _uiState.value.currentRevenue.plus(_uiState.value.currentDessertPrice)
        updateGameState(updatedRevenue)
    }

    private fun updateGameState(updatedRevenue: Int) {
        _uiState.update { currentState ->
            // calculate new dessert index
            var newDessertIndex = 0
            for (i in 0 until currentState.desserts.size) {
                if (currentState.currentDessertsSold >= currentState.desserts[i].startProductionAmount) {
                    newDessertIndex = i
                } else {
                    break
                }
            }

            currentState.copy(
                currentRevenue = updatedRevenue,
                currentDessertsSold = currentState.currentDessertsSold.inc(),
                currentDessertIndex = newDessertIndex,
                currentDessertPrice = currentState.desserts[newDessertIndex].price,
                currentDessertImageId = currentState.desserts[newDessertIndex].imageId
            )
        }
    }

    fun resetGame() {
        _uiState.value = GameUiState()
    }

    init {
        resetGame()
    }
}