# Task 11: Implement, Test and Defend the Design (5 marks)

The complete system, organised in packages (see Task 10):

| Package | Classes |
|---|---|
| `parking.model` | `Vehicle` (abstract), `TwoWheeler`, `Car`, `ElectricCar`, `Chargeable` (interface), `UserCategory` (enum), `ParkingTicket` |
| `parking.service` | `ParkingFacility` (jagged array), `ParkingService` (enter/exit, `HashMap` of active tickets), `VehicleFactory` (type registry) |
| `parking.app` | `ParkingApp` (reads `input/scenario.txt`), `ParkingTests` (test table), `TicketLookupBenchmark` |

**Rules implemented:** every started hour charged in full; daily maximum = 10 hours' worth; two-wheeler Rs 10/h, car and EV Rs 30/h; EV charging Rs 12/kWh, added on top; students and staff with a monthly pass pay Rs 0 for parking; visitors pay full and cannot hold a monthly pass; 0 minutes = Rs 0.

## Run

```bash
cd Task11
javac -d out $(find src -name "*.java")
java -cp out parking.app.ParkingApp input/scenario.txt   # one-day scenario
java -cp out parking.app.ParkingTests                     # test table
java -cp out parking.app.TicketLookupBenchmark            # alternative-concept measurement
```

## 1. Test table: expected vs actual

All 25 tests pass (`output/ParkingTests.png`). Each row is produced by `ParkingTests.java`.

| ID | Category | Test case | Expected | Actual | Result |
|---|---|---|---|---|---|
| N1 | Normal | Car, 130 min (3 started hours × 30) | 90.0 | 90.0 | PASS |
| N2 | Normal | TwoWheeler, 130 min (3 × 10) | 30.0 | 30.0 | PASS |
| N3 | Normal | EV, 130 min + 10 kWh (90 + 120) | 210.0 | 210.0 | PASS |
| N4 | Normal | Staff with monthly pass | 0.0 | 0.0 | PASS |
| N5 | Normal | Exit frees the slot for the next car | [0, 0] | [0, 0] | PASS |
| B1 | Boundary | Last free slot is allocated | [1, 0] | [1, 0] | PASS |
| B2 | Boundary | Facility full → rejected | IllegalStateException | IllegalStateException | PASS |
| B3 | Boundary | Zero-minute parking | 0.0 | 0.0 | PASS |
| B4 | Boundary | Exactly 60 min (slab boundary) | 30.0 | 30.0 | PASS |
| B5 | Boundary | 61 min starts the 2nd hour | 60.0 | 60.0 | PASS |
| B6 | Boundary | 13 h capped at daily max | 300.0 | 300.0 | PASS |
| B7 | Boundary | Overnight 22:00 → 01:00 (3 h) | 90.0 | 90.0 | PASS |
| B8 | Boundary | Ticket numbers unique and consecutive | true | true | PASS |
| I1 | Invalid | Unknown vehicle type "BUS" | IllegalArgumentException | IllegalArgumentException | PASS |
| I2 | Invalid | Duplicate registration while parked | IllegalStateException | IllegalStateException | PASS |
| I3 | Invalid | Release invalid slot (5, 0) | false | false | PASS |
| I4 | Invalid | Release a slot that is already free | false | false | PASS |
| I5 | Invalid | Negative duration | IllegalArgumentException | IllegalArgumentException | PASS |
| I6 | Invalid | Invalid registration "12 34" | IllegalArgumentException | IllegalArgumentException | PASS |
| I7 | Invalid | Visitor claiming a monthly pass | IllegalArgumentException | IllegalArgumentException | PASS |
| I8 | Invalid | Exit for a vehicle not parked | IllegalArgumentException | IllegalArgumentException | PASS |
| I9 | Invalid | Charging a non-EV | IllegalArgumentException | IllegalArgumentException | PASS |
| I10 | Invalid | After a rejected exit, the car is still parked | 1 | 1 | PASS |
| O1 | Object | `Car` equals `ElectricCar` with same regNo | true | true | PASS |
| O2 | Object | Equal objects have equal hashCodes | true | true | PASS |

**A bug found by testing:** the first version of `exit()` removed the ticket from the map *before* checking whether charging was allowed. A rejected exit (I9) therefore left the car's slot occupied while the system had "forgotten" the car. Test **I10** was added, and `exit()` now removes the ticket only after every check passes.

## 2. Small scenario: hand calculation vs program

Facility: 2 floors with 3 and 2 slots (capacity 5). Input: `input/scenario.txt`.

| Time | Event | Slot | Hand calculation | Fee |
|---|---|---|---|---|
| 08:00 | Car TN01AA1111, visitor | F0-S0 | | |
| 08:15 | Two-wheeler TN02BB2222, student + pass | F0-S1 | | |
| 08:30 | EV TN03CC3333, visitor | F0-S2 | | |
| 09:00 | Two-wheeler TN04DD4444, visitor | F1-S0 | | |
| 09:05 | Car TN05EE5555, staff + pass | F1-S1 | (facility now full) | |
| 09:10 | Car TN06FF6666 | | **Rejected: full** | |
| 09:20 | Car TN01AA1111 again | | **Rejected: duplicate** | |
| 09:30 | BUS TN07GG7777 | | **Rejected: unknown type** | |
| 10:00 | TN01AA1111 exits | | 120 min → 2 h × 30 | **60** |
| 10:05 | TN04DD4444 exits | | 65 min → 2 h × 10 | **20** |
| 10:30 | Car TN06FF6666, visitor | F0-S0 (freed) | | |
| 12:15 | TN03CC3333 exits, 20 kWh | | 225 min → 4 h × 30 = 120; 20 × 12 = 240 | **360** |
| 17:00 | TN02BB2222 exits | | monthly pass | **0** |
| 18:00 | TN05EE5555 exits | | monthly pass | **0** |
| 23:30 | TN06FF6666 exits | | 780 min → 13 h × 30 = 390 → capped | **300** |
| 23:45 | TN99ZZ9999 exits | | **Rejected: no active ticket** | |
| | **Total** | | 60 + 20 + 360 + 0 + 0 + 300 | **Rs 740** |

**Program output** (`output/ParkingApp.png`): `Total collection : Rs 740.00`. Every individual fee, slot and rejection matches the hand calculation.

## 3. How well the design follows the four OOP principles

| Principle | Evidence from the implementation | Tested by |
|---|---|---|
| **Encapsulation** | All fields `private`; identity fields `final`; validation in constructors and `close()`; `closedTickets()` returns an unmodifiable list; `ParkingService` is the only path to change occupancy | I3–I7, I10 |
| **Inheritance** | `TwoWheeler`/`Car` reuse the fee algorithm and override one method; `ElectricCar` reuses all of `Car` | N1–N3 |
| **Polymorphism** | `exit()` calls `v.calculateFee(in, out)` with no type checks; the overloads (minutes vs clock times) are chosen at compile time | N1–N3, B7 |
| **Abstraction** | Abstract `Vehicle` with abstract `ratePerHour()`; `Chargeable` interface; `VehicleFactory` hides how vehicles are created from a type name | I1, I9 |

**One limitation:** pass status is a `boolean monthlyPass` on the ticket, and the "pass means free" rule is hard-coded in `ParkingService.exit()`. A new kind of pass (for example, a one-day pass) cannot be added by extension; `ParkingTicket` and `ParkingService` would have to be edited. A second, smaller limitation: allocation is a linear scan, O(F × S), which is fine for 500 slots but not for very large facilities (see Task 3). Task 12 fixes the first limitation.

## 4. Recommendation

**Keep this design.** Evidence: 25 of 25 tests pass, including all boundary and invalid cases; the scenario total matches the hand calculation to the rupee; adding a vehicle type needs one new subclass plus one `VehicleFactory.register(...)` call, with no edits to tested classes. The weak point is the pass rule, which should be turned into an abstraction as soon as a second pass type appears (Task 12).

---

## Alternative Concept and Complexity Analysis: storing active tickets

The system keeps active tickets in a **`HashMap<String, ParkingTicket>`** keyed by registration number. Exit processing with the alternatives:

```java
// Array or ArrayList: linear search on exit
for (ParkingTicket t : tickets)
    if (t.getVehicle().getRegNo().equals(regNo)) { /* found */ }
// remove: array → set to null (leaves a gap) or shift; ArrayList.remove(i) shifts O(n)

// HashMap: direct lookup
ParkingTicket t = active.get(regNo);    // then active.remove(regNo)
```

**My measurement** (`TicketLookupBenchmark`, 20,000 random lookups, `output/TicketLookupBenchmark.png`):

| n parked | Array (ns) | ArrayList (ns) | HashMap (ns) |
|---|---|---|---|
| 100 | ≈ 330 | ≈ 890 | ≈ 110 |
| 500 | ≈ 1,100 | ≈ 1,520 | ≈ 80 |
| 1,000 | ≈ 2,400 | ≈ 2,080 | ≈ 20 |
| 5,000 | ≈ 8,900 | ≈ 10,700 | ≈ 16 |

(Absolute numbers vary between runs and machines. The trend is what matters: array and list times grow with n, and the HashMap stays flat. Small-n HashMap times include JIT warm-up.)

| | Array | ArrayList | HashMap |
|---|---|---|---|
| Find ticket on exit | **O(n)** | **O(n)** | **O(1)** average (O(log n) worst case in Java 8+ with tree bins) |
| Remove on exit | O(1) set null (leaves gaps), O(n) to compact | **O(n)** (shift) | **O(1)** |
| Duplicate-registration check on entry | O(n) | O(n) | **O(1)**: `containsKey` |
| Space for n tickets | n references; **fixed size** (must pre-size to capacity) | n references + up to ~50% spare capacity | n entries × (key ref + value ref + hash + next ref + entry header) ≈ 3–4× the references of an array, plus a table of ~n/0.75 buckets |

**Conclusion:** the HashMap uses more memory per ticket, but with 500 slots that is only a few kilobytes. In exchange, exit and the duplicate check become O(1). Exit is the operation that happens at the busy gate, so the HashMap is the right choice.

## Files
| Path | Purpose |
|---|---|
| `src/parking/**` | Complete source |
| `input/scenario.txt` | One-day scenario input |
| `output/ParkingApp.*` | Scenario run |
| `output/ParkingTests.*` | Test table run |
| `output/TicketLookupBenchmark.*` | Measurement |
