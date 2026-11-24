package parking.model;

/** Multilevel inheritance (Vehicle -> Car -> ElectricCar) plus an interface capability. */
public final class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) { super(regNo); }

    @Override public double chargingFee(int kWh) {
        return Chargeable.validateKWh(kWh) * RATE_PER_KWH;
    }
}
