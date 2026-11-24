package parking.model;

/** Students and staff with a valid monthly pass park free (same rule as Task 11). */
public final class MonthlyPass implements Pass {
    @Override public double apply(double parkingFee) { return 0; }
    @Override public boolean allowedFor(UserCategory category) { return category.canHoldMonthlyPass(); }
    @Override public String name() { return "MONTHLY"; }
}
