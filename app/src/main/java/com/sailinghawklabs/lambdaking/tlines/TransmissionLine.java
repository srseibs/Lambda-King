package com.sailinghawklabs.lambdaking.tlines;

public class TransmissionLine {
    private Double velocityFactor;
    private String description;

    public TransmissionLine(Double velocityFactor, String description) {
        this.velocityFactor = velocityFactor;
        this.description = description;
    }

    public Double getVelocityFactor() {
        return velocityFactor;
    }

    public String getVelocityFactorString() {
        return velocityFactor.toString();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return velocityFactor + " " + description;
    }
}
