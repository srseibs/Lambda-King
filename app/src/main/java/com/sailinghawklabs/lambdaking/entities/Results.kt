package com.sailinghawklabs.lambdaking.entities

open class Results{
    // relative permittivity (epsilon)
    var epsilon_r: Double= 0.0

    // either Epsilon or Vf is set by user, the other is calculated
    var velocityFactor: Double = 0.0

    // velocities
    var velocity_m_s: Double = 0.0
    var velocity_mi_s: Double = 0.0
    var velocity_s_m: Double = 0.0
    var velocity_s_in: Double = 0.0

    // wavelengths
    var lambda_m: Double = 0.0
    var num_wavelens: Double = 0.0

    // phase shift
    var phaseShift_deg: Double = 0.0

    // phase slopes
    var phase_slope_m_deg: Double = 0.0
    var phase_slope_ft_deg: Double = 0.0
    var phase_slope_deg_hz: Double = 0.0

    // delay time
    var delay_s: Double = 0.0

    // VSWR ripple spacing
    var vswr_ripple_spacing_hz = 0.0
}