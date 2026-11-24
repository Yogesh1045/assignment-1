package parking.app;

import parking.model.ParkingTicket;
import parking.model.UserCategory;
import parking.model.Vehicle;
import parking.service.ParkingFacility;
import parking.service.ParkingService;
import parking.service.VehicleFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.util.Scanner;

/** Runs a day's scenario read from a text file: java parking.app.ParkingApp input/scenario.txt */
public class ParkingApp {
    public static void main(String[] args) throws FileNotFoundException {
        String path = args.length > 0 ? args[0] : "input/scenario.txt";
        ParkingService service = null;

        try (Scanner in = new Scanner(new File(path))) {
            while (in.hasNextLine()) {
                String line = in.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split("\\s+");
                try {
                    switch (p[0]) {
                        case "FACILITY" -> {
                            int[] perFloor = new int[p.length - 1];
                            for (int i = 1; i < p.length; i++) perFloor[i - 1] = Integer.parseInt(p[i]);
                            service = new ParkingService(new ParkingFacility("Main", perFloor));
                            System.out.println("Facility created, capacity " + service.getFacility().capacity());
                        }
                        case "ENTER" -> {
                            Vehicle v = VehicleFactory.create(p[2], p[3]);
                            UserCategory cat = UserCategory.valueOf(p[4]);
                            ParkingTicket t = service.enter(v, cat, p[5].equals("PASS"), LocalTime.parse(p[1]));
                            System.out.println(p[1] + " ENTER  " + t);
                        }
                        case "EXIT" -> {
                            double fee = service.exit(p[2], LocalTime.parse(p[1]), Integer.parseInt(p[3]));
                            System.out.printf("%s EXIT   %-12s pays Rs %.2f%n", p[1], p[2], fee);
                        }
                        default -> System.out.println("Unknown command: " + line);
                    }
                } catch (RuntimeException e) {
                    System.out.println("REJECTED  " + line + "  -> " + e.getMessage());
                }
            }
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Still parked      : " + service.activeCount());
        System.out.printf("Total collection  : Rs %.2f%n", service.getTotalCollection());
    }
}
