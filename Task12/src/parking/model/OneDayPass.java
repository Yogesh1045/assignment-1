package parking.model;

/** NEW in Task 12: visitors buy a one-day pass at the gate; parking for that day is then free. */
public final class OneDayPass implements Pass {
    public static final double PRICE = 100;

    @Override public double apply(double parkingFee) { return 0; }
    @Override public double purchasePrice() { return PRICE; }
    @Override public boolean allowedFor(UserCategory category) { return category == UserCategory.VISITOR; }
    @Override public String name() { return "ONE_DAY"; }
}
