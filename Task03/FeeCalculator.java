/**
 * Task 3 - fee computation control logic (the second core operation).
 *
 * Fee slabs (per vehicle type):
 *   - 0 minutes            -> Rs 0 (vehicle left immediately)
 *   - first started hour   -> firstHourRate
 *   - each further started hour -> additionalHourRate
 *   - capped at dailyMax for every started 24-hour day
 *   - holders of a valid monthly pass pay 0
 */
public class FeeCalculator {

    public static final int TWO_WHEELER = 1;
    public static final int CAR = 2;
    public static final int EV = 3;

    public static int computeFee(int vehicleType, int minutes, boolean hasPass) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative: " + minutes);
        }
        if (hasPass || minutes == 0) {
            return 0;                                   // early return: nothing to compute
        }

        int firstHour, extraHour, dailyMax;
        switch (vehicleType) {
            case TWO_WHEELER: firstHour = 10; extraHour = 5;  dailyMax = 60;  break;
            case CAR:         firstHour = 30; extraHour = 20; dailyMax = 300; break;
            case EV:          firstHour = 30; extraHour = 20; dailyMax = 300; break;
            default: throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
        }

        int fullDays = minutes / (24 * 60);             // complete 24-hour days
        int remMinutes = minutes % (24 * 60);
        int hours = (remMinutes + 59) / 60;             // ceiling with integer arithmetic only

        int remFee;
        if (hours == 0) {
            remFee = 0;
        } else if (hours == 1) {
            remFee = firstHour;
        } else {
            remFee = firstHour + (hours - 1) * extraHour;
        }
        if (remFee > dailyMax) {
            remFee = dailyMax;                          // daily maximum slab
        }
        return fullDays * dailyMax + remFee;
    }

    public static void main(String[] args) {
        int[][] cases = {
            {CAR, 0}, {CAR, 1}, {CAR, 60}, {CAR, 61}, {CAR, 130},
            {TWO_WHEELER, 130}, {CAR, 24 * 60}, {CAR, 24 * 60 + 61}, {EV, 600}
        };
        String[] names = {"", "TwoWheeler", "Car", "EV"};
        System.out.println("Type        Minutes  Fee(Rs)");
        for (int[] c : cases) {
            System.out.printf("%-11s %7d  %7d%n", names[c[0]], c[1], computeFee(c[0], c[1], false));
        }
        System.out.println("Car 300 min with monthly pass -> Rs " + computeFee(CAR, 300, true));
    }
}
