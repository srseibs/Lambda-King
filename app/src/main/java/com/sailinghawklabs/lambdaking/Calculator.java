package com.sailinghawklabs.lambdaking;

public class Calculator {
    public static Results execute(double frequency_Hz, double cableLength_m, double velocityFactor, double epsilon, Boolean epsilonMaster) {
        // auto-units: *s, *m, ft|in|mi|mil|yd,

        // (1) Velocity = F * Lambda  [m/s]
        // (2) Velocity = c * Vp [m/s]
        // (3) Lambda = c * Vp  / F  [m]    From (1) and (2)
        // (4) Lambda/2, Lambda/4 [m]       From (3)
        // (5) PhaseShift = 360 * CableLength / Lambda  [m]
        // (6) velocityFactor = 1 / (sqrt(epsilon)    epsilon = 1 / (velocityFactor^2)

        // calculate some basic intermediate results

        assert(epsilon > 0);
        assert(velocityFactor > 0);

        Results temp = new Results();

        temp.velocity_m_s = PhysicalConstants.SPEED_OF_LIGHT_mps * velocityFactor;
        temp.velocity_mi_s = temp.velocity_m_s /  (PhysicalConstants.FEET_PER_MILE_ft * PhysicalConstants.FEET_PER_METER_ft);
        temp.velocity_s_m = 1/temp.velocity_m_s;
        temp.velocity_s_in =  temp.velocity_s_m / (12 * PhysicalConstants.FEET_PER_METER_ft);

        temp.lambda_m = temp.velocity_m_s / frequency_Hz;
        temp.phaseShift_deg = 360.0 * cableLength_m / temp.lambda_m ;
        temp.delay_s = cableLength_m / temp.velocity_m_s;

        //  https://www.microwaves101.com/encyclopedias/cable-length-rule-of-thumb
        temp.vswr_ripple_spacing_hz = temp.velocity_m_s / (2.0 * cableLength_m);

        temp.num_wavelens = cableLength_m / temp.lambda_m;
        temp.phase_slope_m_deg = cableLength_m / temp.phaseShift_deg;
        temp.phase_slope_ft_deg = temp.phase_slope_m_deg * PhysicalConstants.FEET_PER_METER_ft;
        temp.phase_slope_deg_hz = temp.phaseShift_deg / frequency_Hz;


        if (epsilonMaster) {
            temp.epsilon_r = epsilon;
            temp.velocityFactor = 1 / (Math.sqrt(epsilon));
        } else {
            temp.epsilon_r = 1 / (velocityFactor * velocityFactor);
            temp.velocityFactor = velocityFactor;
        }

        return temp;
    }
}
