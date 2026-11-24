package parking.model;

/** NEW in Task 12: e-bikes pay a flat fee for each started day, whatever the duration. */
public class EBike extends Vehicle {
    public static final double FLAT_FEE = 20;

    public EBike(String regNo) { super(regNo); }

    /** Not used for billing - kept because Vehicle requires it; equals the flat fee. */
    @Override protected double ratePerHour() { return FLAT_FEE; }

    @Override public double calculateFee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration: " + minutes);
        if (minutes == 0) return 0;
        int startedDays = (minutes + 24 * 60 - 1) / (24 * 60);
        return startedDays * FLAT_FEE;
    }
}
