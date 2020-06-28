package com.sailinghawklabs.lambdaking.entities

object FreqUnits : LinkedHashMap<Double, String>() {
    val defaultUnit = "MHz"
    val defaultUnitIndex : Int

    init {
        this[1e15] = "PHz"
        this[1e12] = "THz"
        this[1e9] = "GHz"
        this[1e6] = "MHz"
        this[1e3] = "kHz"
        this[1.0] = "Hz"
        this[1e-3] = "mHz"
        this[1e-6] = "uHz"

        defaultUnitIndex = values.indexOf(defaultUnit)
    }
}