package com.sailinghawklabs.lambdaking;

public class Results {

    // relative permittivity (epsilon)
    Double epsilon_r;
    // either Epsilon or Vf is set by user, the other is calculated
    Double velocityFactor;

    // velocities
    Double velocity_m_s;
    Double velocity_mi_s;
    Double velocity_s_m;
    Double velocity_s_in;

    // wavelengths
    Double lambda_m;
    Double num_wavelens;

    // phase shift
    Double phaseShift_deg;

    // phase slopes
    Double phase_slope_m_deg;
    Double phase_slope_ft_deg;
    Double phase_slope_deg_hz;

    // delay time
    Double delay_s;

    // VSWR ripple spacing
    Double vswr_ripple_spacing_hz;

}
