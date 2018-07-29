package com.sailinghawklabs.lambdaking;

import com.sailinghawklabs.engineeringnotation.EngineeringNotationTools;

public class RangedValue {
    private Double actualValue;

    private String engineeringMantissa;
    private String baseUnit;
    private String unitMultiplier;


    public RangedValue(Double value, String engineeringMantissa, String baseUnits) {
        this.actualValue = value;
        this.engineeringMantissa = engineeringMantissa;
        this.baseUnit = baseUnits;
        this.unitMultiplier = "";
    }


    public RangedValue(EngineeringNotationTools.MantissaExponent mantExp, Double value, String baseUnits) {
        actualValue = value;
        engineeringMantissa = mantExp.mantissaString;
        unitMultiplier = mantExp.exponentString;
        baseUnit = baseUnits;
    }

    public String toEngineeringString() {
        return engineeringMantissa + " " + unitMultiplier + baseUnit;
    }

    public String getEngineeringMantissa() {
        return engineeringMantissa;
    }

    public String getUnitMuliplier() {
        return unitMultiplier;
    }

    public String getBaseUnit() {
        return baseUnit;
    }

    public Double getValue() {
        return actualValue;
    }

}
