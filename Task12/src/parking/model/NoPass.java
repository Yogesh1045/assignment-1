package parking.model;

/** Pays the full fee. A single shared instance is enough because it has no state. */
public final class NoPass implements Pass {
    static final NoPass INSTANCE = new NoPass();

    private NoPass() { }

    @Override public double apply(double parkingFee) { return parkingFee; }
    @Override public String name() { return "NOPASS"; }
}
