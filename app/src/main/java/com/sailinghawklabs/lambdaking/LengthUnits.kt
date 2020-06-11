package com.sailinghawklabs.lambdaking

object LengthUnits : LinkedHashMap<Double, String>() {
    // populate map with value multipliers to yield m

    val defaultUnit = "m"
    val defaultUnitIndex : Int
    init {
        this[1e3] = "km"
        this[1.0] = "m"
        this[1e-1] = "cm"
        this[1e-3] = "mm"
        this[1 / (1000 * 12 * PhysicalConstants.FEET_PER_METER_ft)] = "mil"
        this[1 / (12 * PhysicalConstants.FEET_PER_METER_ft)] = "in"
        this[1 / PhysicalConstants.FEET_PER_METER_ft] = "ft"

        defaultUnitIndex = values.indexOf(defaultUnit)
    }
}