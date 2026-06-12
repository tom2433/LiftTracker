package com.example.lifttracker.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Data model for Sport
 */
data class Sport(
    val id: Int,
    @field:StringRes val titleResourceId: Int,
    @field:StringRes val subtitleResourceId: Int,
    val playerCount: Int,
    val olympic: Boolean,
    @field:DrawableRes val imageResourceId: Int,
    @field:DrawableRes val sportsImageBanner: Int,
    @field:StringRes val sportDetails: Int
)