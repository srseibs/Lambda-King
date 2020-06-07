package com.sailinghawklabs.lambdaking

import android.util.Log
import com.sailinghawklabs.engineeringnotation.EngineeringNotationTools
import java.util.*

class AutoRanger(private var mNumDps: Int = DEFAULT_NUM_DIGITS) {

    fun rangePhase(phaseValue: Double?): RangedValue {
        val rangedPhase = EngineeringNotationTools.encodeMantissa(phaseValue!!, mNumDps)
        return RangedValue(rangedPhase, phaseValue, "deg")
    }

    fun rangeTime(timeValue: Double?): RangedValue {
        val rangedPhase = EngineeringNotationTools.encodeMantissa(timeValue!!, mNumDps)
        return RangedValue(rangedPhase, timeValue, "s")
    }

    fun rangeWavelengths(wavelengths: Double?): RangedValue {
        val rangedLambdas = EngineeringNotationTools.encodeMantissa(wavelengths!!, mNumDps)
        return RangedValue(rangedLambdas, wavelengths, "" + Characters.LAMBDA)
    }

    fun rangeLength(length: Double?): RangedValue {
        val rangedLength = EngineeringNotationTools.encodeMantissa(length!!, mNumDps)
        return RangedValue(rangedLength, length, "m")
    }

    fun rangeLengthImperial(length_m: Double): RangedValue {
        val length_ft = length_m * PhysicalConstants.FEET_PER_METER_ft
        val length_mi = length_ft / PhysicalConstants.FEET_PER_MILE_ft
        val length_in = length_ft * 12
        val length_mil = length_ft * 12 * 1000

        Log.d("AutoRanger", "rangeLengthImperial: input(m)=$length_m, mi=$length_mi, ft=$length_ft, in=$length_in, mil=$length_mil")
        val units : String
        val formatString = "%." + mNumDps + "f"
        val lengthStr : String
        if (length_mi > 1) {
            lengthStr = String.format(Locale.US, formatString, length_mi)
            units = "mi"
        } else if (length_ft > 10) {
            lengthStr = String.format(Locale.US, formatString, length_ft)
            units = "ft"
        } else if (length_in > 1) {
            lengthStr = String.format(Locale.US, formatString, length_in)
            units = "in"
        } else {
            lengthStr = String.format(Locale.US, formatString, length_mil)
            units = "mil"
        }
        return RangedValue(length_ft, lengthStr, units)
    }

    companion object {
        private const val DEFAULT_NUM_DIGITS = 4
    }

}