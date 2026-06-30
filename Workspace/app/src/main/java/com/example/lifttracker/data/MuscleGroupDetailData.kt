package com.example.lifttracker.data

// This Room projection contains the stored values and aggregate values needed to build a muscle-group detail card. - Codex
data class MuscleGroupDetailData(
    val id: Int,
    val name: String,
    val note: String,
    val numLifts: Int,
    val numSessions: Int,
    val numSets: Int,
    val numRepLifts: Int,
    val avgNumRepsPerSet: Double?,
    val firstDateTrained: String?,
    val lastDateTrained: String?
)
