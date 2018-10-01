package com.sailinghawklabs.lambdaking;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CheckCalculations {
    public static final double DEF_TOLERANCE_PCT = 0.0001;



    void checkValue(String message, double value, double expected) {
        checkValue(message, value, expected, DEF_TOLERANCE_PCT);
    }
    void checkValue(String msg, double value, double expected, double tolerancePct) {
        double tolerance = (tolerancePct/100) * expected;
        String message = msg + ": value = " + Double.toString(value) + ", expected = " + Double.toString(expected) + ", tolerance = " + Double.toString(tolerance);
        
        assertTrue("< " + message, value < (expected + tolerance));
        assertTrue("> " + message, value > (expected - tolerance));
    }

    @Test
    public void test_1() {
        Results result = Calculator.execute(3.0000000E+08, 1, 1);
        checkValue("V", result.velocity_m_s, 2.9979246E+08);
        checkValue("wavelength", result.lambda_m, 9.9930819E-01);
        checkValue("num Wavelengths", result.num_wavelens, 1.0006923E+00);
        checkValue("delay", result.delay_s,3.3356410E-09);
        checkValue("epsilon", result.epsilon_r, 1.000000000);
        checkValue("VSWR ripple", result.vswr_ripple_spacing_hz, 1.4989623E+08);
        checkValue("Phase shift", result.phaseShift_deg, 3.6024922E+02);
    }

    @Test
    public void test_2() {
        Results result = Calculator.execute(1.0000000E+08, 1, 6.9006556E-01);
        checkValue("V", result.velocity_m_s, 2.0687645E+08);
        checkValue("wavelength", result.lambda_m, 2.0687645E+00);
        checkValue("num Wavelengths", result.num_wavelens, 4.8338030E-01);
        checkValue("delay", result.delay_s,4.8338030E-09);
        checkValue("epsilon", result.epsilon_r, 2.1000000E+00);
        checkValue("VSWR ripple", result.vswr_ripple_spacing_hz, 1.0343823E+08);
        checkValue("Phase shift", result.phaseShift_deg, 1.7401691E+02);

    }
}