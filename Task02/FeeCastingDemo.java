import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Task 2 - data types and casting risks in billing.
 * Reads stays from input.txt with Scanner (or from command-line args if given) and shows
 * how integer division and a wrong cast give a wrong fee.
 *
 * Run:  java FeeCastingDemo            (reads input.txt)
 *       java FeeCastingDemo 130 30     (minutes, rate from the command line)
 */
public class FeeCastingDemo {

    /** WRONG #1: integer division truncates 130/60 to 2 -> one started hour is lost. */
    static int wrongFeeIntegerDivision(int minutes, int rate) {
        int hours = minutes / 60;
        return hours * rate;
    }

    /** WRONG #2: Math.ceil applied AFTER integer division - (double)(130/60) is already 2.0. */
    static int wrongFeeLateCast(int minutes, int rate) {
        int hours = (int) Math.ceil(minutes / 60);
        return hours * rate;
    }

    /** WRONG #3: fractional hours -> charges for 2.1666 hours instead of 3 started hours. */
    static double wrongFeeFractional(int minutes, double rate) {
        return (minutes / 60.0) * rate;
    }

    /** CORRECT: integer ceiling without any floating point. */
    static int correctFee(int minutes, int rate) {
        int hours = (minutes + 59) / 60;
        return hours * rate;
    }

    /** Also correct: cast BEFORE dividing so the division is done in double. */
    static int correctFeeWithCast(int minutes, int rate) {
        int hours = (int) Math.ceil(minutes / 60.0);
        return hours * rate;
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.printf("%-8s %-5s %-10s %-10s %-11s %-8s %-8s%n",
                "minutes", "rate", "int-div", "late-cast", "fractional", "correct", "cast-ok");

        if (args.length == 2) {
            print(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            return;
        }
        try (Scanner sc = new Scanner(new File("input.txt"))) {
            while (sc.hasNextInt()) {
                int minutes = sc.nextInt();
                int rate = sc.nextInt();
                print(minutes, rate);
            }
        }

        // Overflow risk: storing a long epoch-millisecond time in an int
        long entryMillis = 1_764_000_000_000L;           // a realistic 2025 timestamp
        int truncated = (int) entryMillis;
        System.out.println("\nlong millis " + entryMillis + " cast to int -> " + truncated
                + "  (overflow - so times are stored as LocalTime / long, never int millis)");

        // Floating point drift: 0.1 + 0.2 for money
        System.out.println("0.1 + 0.2 = " + (0.1 + 0.2)
                + "  (fees are whole rupees, so int/double of whole values is exact)");
    }

    private static void print(int minutes, int rate) {
        System.out.printf("%-8d %-5d %-10d %-10d %-11.2f %-8d %-8d%n", minutes, rate,
                wrongFeeIntegerDivision(minutes, rate), wrongFeeLateCast(minutes, rate),
                wrongFeeFractional(minutes, rate), correctFee(minutes, rate),
                correctFeeWithCast(minutes, rate));
    }
}
