package com.sailinghawklabs.lambdaking.entities

import com.google.gson.Gson

data class EntryState(
        var freqString: String = "",
        var lengthString: String = "",
        var velocityFactorString: String = "",
        var lengthSpinnerIndex: Int = 0,
        var freqUnitsSpinnerIndex: Int = 0) {

    fun serialize() : String {
        return Gson().toJson(this)
    }

    companion object {
        fun create(serializedData: String) : EntryState {
            return Gson().fromJson(serializedData, EntryState::class.java)
        }

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