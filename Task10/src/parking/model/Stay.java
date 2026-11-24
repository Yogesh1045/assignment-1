package parking.model;

/** Duration of a stay. The setter rejects impossible values so a Stay is always valid. */
public class Stay {
    private int minutes;
    private Integer kWhCharged;                       // wrapper: null means "did not charge"

    public Stay(int minutes) { setMinutes(minutes); }

    public int getMinutes() { return minutes; }

    public void setMinutes(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Duration cannot be negative: " + minutes);
        if (minutes > 7 * 24 * 60) throw new IllegalArgumentException("Stay longer than 7 days: " + minutes);
        this.minutes = minutes;
    }

    public Integer getKWhCharged() { return kWhCharged; }

    public void setKWhCharged(Integer kWh) {
        if (kWh != null && kWh < 0) throw new IllegalArgumentException("kWh cannot be negative");
        this.kWhCharged = kWh;                         // autoboxing happens at the call site
    }

    /** Package-private: only classes in parking.model may use this helper. */
    int startedHours() { return (minutes + 59) / 60; }

    /** Protected: available to subclasses (e.g. a future OvernightStay) and the package. */
    protected boolean isOvernight() { return minutes >= 12 * 60; }

    public int billableHours() { return startedHours(); }
}
