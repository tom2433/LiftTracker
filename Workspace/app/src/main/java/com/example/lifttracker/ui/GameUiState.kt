package com.example.lifttracker.ui

import com.example.lifttracker.model.Dessert
import com.example.lifttracker.data.Datasource.dessertList

data class GameUiState(
    val currentRevenue: Int = 0,
    val currentDessertsSold: Int = 0,
    val currentDessertIndex: Int = 0,
    val currentDessertPrice: Int = dessertList[currentDessertIndex].price,
    val currentDessertImageId: Int = dessertList[currentDessertIndex].imageId,
    val desserts: List<Dessert> = dessertList
)
