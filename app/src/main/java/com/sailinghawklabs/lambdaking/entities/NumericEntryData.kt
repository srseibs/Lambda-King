package com.sailinghawklabs.lambdaking.entities

data class NumericEntryData(
        var valid: Boolean = true,
        var frequency_Hz: Double = 1.0,
        var cableLength_m: Double = 1.0,
        var velocityFactor: Double = 1.0,
        var epsilon: Double = 1.0
)