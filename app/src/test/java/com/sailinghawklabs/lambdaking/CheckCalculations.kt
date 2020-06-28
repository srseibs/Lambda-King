package com.sailinghawklabs.lambdaking

import com.sailinghawklabs.lambdaking.Calculator.execute
import com.sailinghawklabs.lambdaking.entities.NumericEntryData
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class CheckCalculations {
    fun checkValue(message: String, value: Double, expected: Double) {
        checkValue(message, value, expected, DEF_TOLERANCE_PCT)
    }

    fun checkValue(msg: String, value: Double, expected: Double, tolerancePct: Double) {
        val tolerance = tolerancePct / 100 * expected
        val message = msg + ": value = " + java.lang.Double.toString(value) + ", expected = " + java.lang.Double.toString(expected) + ", tolerance = " + java.lang.Double.toString(tolerance)
        Assert.assertTrue("< $message", value < expected + tolerance)
        Assert.assertTrue("> $message", value > expected - tolerance)
    }

    @Test
    fun test_1() {
        val entry = NumericEntryData(frequency_Hz = 3.0000000E+08, cableLength_m = 1.0, velocityFactor = 1.0)
        val result = execute(entry, epsilonMaster = false)
        checkValue("V", result.velocity_m_s, 2.9979246E+08)
        checkValue("wavelength", result.lambda_m, 9.9930819E-01)
        checkValue("num Wavelengths", result.num_wavelens, 1.0006923E+00)
        checkValue("delay", result.delay_s, 3.3356410E-09)
        checkValue("epsilon", result.epsilon_r, 1.000000000)
        checkValue("VSWR ripple", result.vswr_ripple_spacing_hz, 1.4989623E+08)
        checkValue("Phase shift", result.phaseShift_deg, 3.6024922E+02)
    }

    @Test
    fun test_2() {
        val entry = NumericEntryData(frequency_Hz = 1.0000000E+08, cableLength_m = 1.0, velocityFactor = 6.9006556E-01)
        val result = execute(entry, epsilonMaster = false)
        checkValue("Speed", result.velocity_m_s, 2.0687645E+08)
        checkValue("wavelength", result.lambda_m, 2.0687645E+00)
        checkValue("num Wavelengths", result.num_wavelens, 4.8338030E-01)
        checkValue("delay", result.delay_s, 4.8338030E-09)
        checkValue("epsilon", result.epsilon_r, 2.1000000E+00)
        checkValue("VSWR ripple", result.vswr_ripple_spacing_hz, 1.0343823E+08)
        checkValue("Phase shift", result.phaseShift_deg, 1.7401691E+02)
    }

    @Test
    fun test_3() {
        val entry = NumericEntryData(frequency_Hz = 1.0000000E+08, cableLength_m = 1.0, epsilon = 2.100000)
        val result = execute(entry, epsilonMaster = true)
        checkValue("delay", result.delay_s, 4.8338030E-09)
        checkValue("num Wavelengths", result.num_wavelens, 4.8338030E-01)
        checkValue("Speed", result.velocity_m_s, 2.0687645E+08)
        checkValue("wavelength", result.lambda_m, 2.0687645E+00)
        checkValue("velocity factor", result.velocityFactor, 6.9006556E-01)
        checkValue("VSWR ripple", result.vswr_ripple_spacing_hz, 1.0343823E+08)
        checkValue("Phase shift", result.phaseShift_deg, 1.7401691E+02)
    }

    companion object {
        const val DEF_TOLERANCE_PCT = 0.0001
    }
}