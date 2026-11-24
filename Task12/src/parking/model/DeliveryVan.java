package parking.model;

import java.time.LocalTime;

/**
 * NEW in Task 12: delivery vans pay the car rate, doubled for every started hour that
 * begins between 09:00 and 16:59 (the 9 AM - 5 PM peak).
 */
public class DeliveryVan extends Vehicle {
    public static final double BASE_RATE = 30;            // same as Car
    public static final LocalTime PEAK_START = LocalTime.of(9, 0);
    public static final LocalTime PEAK_END = LocalTime.of(17, 0);

    public DeliveryVan(String regNo) { super(regNo); }

    @Override protected double ratePerHour() { return BASE_RATE; }

    @Override protected double dailyMaximum() { return BASE_RATE * 2 * 10; }   // 600

    /** Overrides the clock-time overload, because the van's price depends on WHEN it parks. */
    @Override public double calculateFee(LocalTime in, LocalTime out) {
        int minutes = (int) java.time.Duration.between(in, out).toMinutes();
        if (minutes < 0) minutes += 24 * 60;
        int hours = (minutes + 59) / 60;
        double fee = 0;
        LocalTime hourStart = in;
        for (int h = 0; h < hours; h++) {
            boolean peak = !hourStart.isBefore(PEAK_START) && hourStart.isBefore(PEAK_END);
            fee += peak ? BASE_RATE * 2 : BASE_RATE;
            hourStart = hourStart.plusHours(1);
        }
        return Math.min(fee, dailyMaximum());
    }
}
