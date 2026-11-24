package parking.model;

/** NEW in Task 12: replaces the old 'boolean monthlyPass'. Any pass type is one more class. */
public interface Pass {
    /** Parking fee actually payable for one stay under this pass. */
    double apply(double parkingFee);

    /** Amount collected when the pass is bought at the gate (0 if bought elsewhere). */
    default double purchasePrice() { return 0; }

    /** May a user of this category hold this pass? */
    default boolean allowedFor(UserCategory category) { return true; }

    String name();

    /** Static factory for the common "no pass" case. */
    static Pass none() { return NoPass.INSTANCE; }
}
