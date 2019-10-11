package com.github.schuettec.multiprocman;

public class ExponentialMovingAverage {
    private static final double DEFAULT_ALPHA = 0.25;
    private double alpha;
    private Double oldValue;

    public ExponentialMovingAverage() {
        this.alpha = DEFAULT_ALPHA;
    }

    public ExponentialMovingAverage(double alpha) {
        this.alpha = alpha;
    }

    /**
     * @return Returns the current average.
     */
    public double average() {
        return this.oldValue;
    }

    /**
     * Calculates a new average and returns this value.
     */
    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;
    }
}