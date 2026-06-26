package com.example.lifttracker.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifttracker.data.Profile
import com.example.lifttracker.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for LiftTrackerDrawer
 */
class DrawerViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _drawerUiState = MutableStateFlow(DrawerUiState())
    val drawerUiState: StateFlow<DrawerUiState> = _drawerUiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream().collect { profiles ->
                _drawerUiState.update { currentState ->
                    currentState.copy(
                        profileList = profiles
                    )
                }
            }
        }
    }

    fun initializeDrawerOffset() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                hasInitializedDrawerOffset = true
            )
        }
    }

    fun toggleDrawer() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = !currentState.isDrawerOpen
            )
        }
    }

    fun closeDrawer() {
        _drawerUiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = false
            )
        }
    }

    fun updateDrawerWidthPx(newVal: Float) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                drawerWidthPx = newVal
            )
        }
    }

    fun updateIsDrawerOpen(newVal: Boolean) {
        _drawerUiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = newVal
            )
        }
    }
}

/**
 * Ui State for SettingsScreen
 */
data class DrawerUiState(
    val profileList: List<Profile> = listOf(),
    val isDrawerOpen: Boolean = false,
    val drawerWidthPx: Float = 0f,
    val hasInitializedDrawerOffset: Boolean = false
)