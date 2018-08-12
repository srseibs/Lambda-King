package com.sailinghawklabs.lambdaking;

import com.sailinghawklabs.engineeringnotation.EngineeringNotationTools;

import java.util.Locale;

public class AutoRanger {

    private static final int DEFAULT_NUM_DIGITS = 4;
    private int mNumDps;

    public AutoRanger() {
        this(DEFAULT_NUM_DIGITS);
    }

    public AutoRanger(int numDps) {
        this.mNumDps = numDps;
    }

    public void setNumDps(int numDps) {
        this.mNumDps = numDps;
    }

    RangedValue rangePhase(Double phaseValue) {
        EngineeringNotationTools.MantissaExponent rangedPhase = EngineeringNotationTools.encodeMantissa(phaseValue, mNumDps);
        return new RangedValue(rangedPhase, phaseValue, "deg");
    }

    RangedValue rangeTime(Double timeValue) {
        EngineeringNotationTools.MantissaExponent rangedPhase = EngineeringNotationTools.encodeMantissa(timeValue, mNumDps);
        return new RangedValue(rangedPhase, timeValue, "s");
    }

    RangedValue rangeWavelengths(Double wavelengths) {
        EngineeringNotationTools.MantissaExponent rangedLambdas = EngineeringNotationTools.encodeMantissa(wavelengths, mNumDps);
        return new RangedValue(rangedLambdas, wavelengths, "" + Characters.LAMBDA);
    }

    RangedValue rangeLength(Double length) {
        EngineeringNotationTools.MantissaExponent rangedLength = EngineeringNotationTools.encodeMantissa(length, mNumDps);
        return new RangedValue(rangedLength, length, "m");
    }

    RangedValue rangeLengthImperial(Double length_m) {
        Double length_ft = length_m * PhysicalConstants.FEET_PER_METER_ft;


        Double length_mi = length_ft / PhysicalConstants.FEET_PER_MILE_ft;
        Double length_in = length_ft * 12;
        Double length_mil = length_ft * 12 * 1000;

        String units = "";
        String formatString = "%." + mNumDps + "f";
        String lengthStr = "";
        if (length_mi > 1) {
            lengthStr = String.format(Locale.US, formatString, length_mi);
            units = "mi";
        } else if (length_ft > 10) {
            lengthStr = String.format(Locale.US, formatString, length_ft);
            units = "ft";
        } else if (length_in > 1) {
            lengthStr = String.format(Locale.US, formatString, length_in);
            units = "in";
        } else {
            lengthStr = String.format(Locale.US, formatString, length_mil);
            units = "mil";
        }
        return new RangedValue(length_ft, lengthStr, units);
    }

}