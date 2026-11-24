package parking.model;

/** Capability of vehicles that can use a charging slot. Charged separately from parking. */
public interface Chargeable {
    double RATE_PER_KWH = 12;          // implicitly public static final

    double chargingFee(int kWh);

    /** Static helper: validates the energy drawn before billing. */
    static int validateKWh(int kWh) {
        if (kWh < 0) throw new IllegalArgumentException("kWh cannot be negative: " + kWh);
        return kWh;
    }
}
