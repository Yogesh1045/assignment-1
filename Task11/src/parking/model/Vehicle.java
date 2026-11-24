package parking.model;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Abstract base of every vehicle. Holds the state all vehicles share (registration number)
 * and the fee algorithm; subclasses only supply their hourly rate.
 */
public abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || !regNo.matches("[A-Z]{2}[0-9A-Z]{2,8}")) {
            throw new IllegalArgumentException("Invalid registration number: " + regNo);
        }
        this.regNo = regNo;
    }

    public final String getRegNo() { return regNo; }

    /** Rate charged for every started hour. */
    protected abstract double ratePerHour();

    /** Maximum charged for one 24-hour day (default: 10 hours' worth). */
    protected double dailyMaximum() { return ratePerHour() * 10; }

    /** Every started hour is charged in full, capped at the daily maximum. */
    public double calculateFee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration: " + minutes);
        int fullDays = minutes / (24 * 60);
        int rest = minutes % (24 * 60);
        int hours = (rest + 59) / 60;                       // integer ceiling, no casting
        double restFee = Math.min(hours * ratePerHour(), dailyMaximum());
        return fullDays * dailyMaximum() + restFee;
    }

    /** Overload (compile-time polymorphism): fee from clock times on the same day / overnight. */
    public double calculateFee(LocalTime in, LocalTime out) {
        long minutes = Duration.between(in, out).toMinutes();
        if (minutes < 0) minutes += 24 * 60;               // left after midnight
        return calculateFee((int) minutes);
    }

    @Override public final boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Vehicle && regNo.equals(((Vehicle) o).regNo);
    }

    @Override public final int hashCode() { return regNo.hashCode(); }

    @Override public String toString() {
        return getClass().getSimpleName() + "[" + regNo + "]";
    }
}
