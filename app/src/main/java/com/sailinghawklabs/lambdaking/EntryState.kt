package com.sailinghawklabs.lambdaking

data class EntryState(
        var freqString: String = "",
        var lengthString: String = "",
        var velocityFactorString: String = "",
        var lengthSpinnerIndex: Int = 0,
        var freqUnitsSpinnerIndex: Int = 0) {

    companion object {
        fun getDefault(): EntryState {
            return EntryState(
                    "1.0",
                    "1.0",
                    "1.0",
                    LengthUnits.defaultUnitIndex,
                    FreqUnits.defaultUnitIndex
            )
        }
    }
}