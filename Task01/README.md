# Task 1: Understand the Problem and Identify the Objects (4 marks)

## 1. Inputs and outputs

| Inputs the system receives | Outputs it must produce |
|---|---|
| Facility layout: number of floors and slots on each floor | A parking ticket: unique ticket number, slot (floor and number), entry time |
| Vehicle arriving: registration number, vehicle type (two-wheeler, car, EV) | "Facility full" message when no slot is free |
| Driver category: student, staff or visitor, and whether they hold a valid monthly pass | Fee payable on exit (parking fee plus EV charging fee) |
| Entry time and exit time | Released slot available for the next vehicle |
| kWh drawn by an EV that used a charging slot | Rejection messages for invalid input (unknown type, duplicate registration, invalid slot) |
| | Day summary: occupancy and total collection |

## 2. Candidate classes (nouns) and methods (verbs)

**Nouns in the problem statement:** campus, parking facility, floor, slot, vehicle, two-wheeler, car, electric vehicle, charging slot, parking ticket, ticket number, entry time, fee, vehicle type, duration, student, staff, visitor, monthly pass.

**Verbs:** enter, allocate a slot, issue a ticket, record entry time, leave/exit, release a slot, compute the fee, charge.

| Candidate class | Why it is needed (justification) | Responsibilities | Methods (verbs) |
|---|---|---|---|
| `Vehicle` (abstract) | Every vehicle has a registration number and a fee, but the fee rule differs by type. A common supertype lets the facility treat all vehicles the same way. | Hold the registration number; define how a fee is calculated from the duration | `getRegNo()`, `calculateFee(minutes)` |
| `TwoWheeler`, `Car` | They differ only in rate. Each is a separate class so a new rate never means editing an if/else chain. | Supply their hourly rate | `ratePerHour()` |
| `ElectricCar` | An EV **is a** car (same parking rate) but can also charge, which is billed separately. | Add the charging fee | `chargingFee(kWh)` |
| `Chargeable` (interface) | Charging is a *capability*, not a kind of vehicle. An interface lets any future vehicle (e-scooter, e-bus) add it. | Declare the charging contract | `chargingFee(kWh)` |
| `ParkingTicket` | The problem talks about a ticket with a *unique number* and an *entry time* that must be remembered until exit. Something has to hold that link between vehicle, slot and time. | Hold the ticket number, vehicle, slot, entry/exit time and fee; generate unique numbers | `close(exitTime, fee)`, getters |
| `ParkingFacility` | Floors and slots are a structure with rules (a slot holds at most one vehicle). That state needs one owner. | Store floors × slots; find the first free slot; release a slot; report capacity | `allocate(vehicle)`, `release(floor, slot)` |
| `UserCategory` (enum) | Student / staff / visitor is a fixed set of values that decides who may hold a pass. An enum prevents typos like "Vistor". | Know which categories may hold a monthly pass | `canHoldMonthlyPass()` |
| `ParkingService` | Entry and exit are *processes* that coordinate several objects (facility, ticket, vehicle, fee). Putting them in `Vehicle` or `ParkingFacility` would give those classes unrelated jobs. | Process entry and exit; keep active tickets; total the day's collection | `enter(...)`, `exit(...)` |

**Nouns that did not become classes, and why:**
- *Floor* and *slot* are positions in the facility's 2-D array, not objects. They have no behaviour of their own in the base problem. A `ParkingSlot` class is shown in Task 10 for validation.
- *Fee*, *duration* and *ticket number* are single values (`double` and `int`), so they are fields.
- *Campus* is the setting; there is only one, so nothing needs it as an object.
- *Charging slot* is treated as an ordinary slot plus a kWh reading at exit (see assumptions).

## 3. Business rules every valid operation must satisfy

1. A slot holds **at most one** vehicle at a time.
2. A ticket number is **never reused**, not even after the vehicle leaves, and not across two gates (static counter, Task 5).
3. A registration number can have **only one active ticket**, so the same car cannot be parked twice.
4. A vehicle can exit only if it has an active ticket; exiting releases exactly that ticket's slot.
5. The fee is never negative; **every started hour is charged in full** (61 minutes = 2 hours); 0 minutes = Rs 0.
6. Only **students and staff** may hold a monthly pass; pass holders pay Rs 0 for parking. **Visitors pay the full fee.**
7. The EV charging fee is added **on top of** parking, even for pass holders, because charging is "charged separately".
8. Charging can be billed only to a vehicle that is `Chargeable`.
9. If no slot is free, the vehicle is turned away and no ticket is issued.

## 4. Assumptions

- Slots are generic: any slot fits any vehicle type. EV charging is recorded as kWh at exit, not as a separate slot type.
- Slots are allocated **lowest floor first, lowest slot first**.
- Rates: two-wheeler Rs 10/hour, car and EV Rs 30/hour, charging Rs 12/kWh (taken from the code given in Task 8). Daily maximum is 10 hours' worth.
- Times are clock times on one day; an exit earlier than the entry means the vehicle stayed past midnight.
- Registration numbers are upper-case letters and digits (for example, TN01AB1234).
- Pass validity (expiry date) is checked by the university's pass office; the system receives only "has valid pass: yes/no".

---

## Alternative Concept and Complexity Analysis: objects vs parallel arrays

**Parallel-array design:**
```java
String[] regNo = new String[MAX];
String[] type = new String[MAX];
int[] entryTime = new int[MAX];   // minutes since midnight
int count = 0;

int find(String target) {
    for (int i = 0; i < count; i++)
        if (regNo[i].equals(target)) return i;   // the index links the three arrays
    return -1;
}
// fee: switch (type[i]) { case "CAR": ... }   -- type logic spread across the program
```

**Object design:**
```java
Vehicle[] vehicles; // or Map<String, Vehicle>
Vehicle find(String target) {
    for (Vehicle v : vehicles) if (v.getRegNo().equals(target)) return v;
    return null;
}
// fee: v.calculateFee(minutes)  -- each object knows its own rule
```

| Criterion | Parallel arrays | Objects |
|---|---|---|
| Find by registration number | Linear search **O(n)** | Linear search **O(n)**; **O(1)** average with `HashMap<String, Vehicle>` |
| Space per vehicle | 2 references + 1 int, **no object header**, but stored in 3 separate arrays | One object: header (~12–16 bytes) + 2 references + 1 int, plus 1 reference in the array |
| Total space | O(n) | O(n), with a slightly larger constant |
| Adding a new attribute (for example, `ownerPhone`) | Add a new array, and update **every** insert, delete and swap/sort to keep the arrays aligned. Missing one corrupts the data silently. | Add **one field** in the class. Code that does not use it is unchanged. |
| Adding a new vehicle type | Edit every `switch(type[i])` | Add one subclass |
| Risk | Arrays can go out of step (the index no longer refers to the same vehicle) | The object keeps its data together |

**My measurement** (`RepresentationCompare.java`, n = 5,000 vehicles, 20,000 random searches; see `output/RepresentationCompare.png`):

| Structure | Average search time |
|---|---|
| `Vehicle[]` linear search | ≈ 7,300 ns |
| Parallel arrays linear search | ≈ 8,100 ns |
| `HashMap<String, Vehicle>` | ≈ 150 ns |

Both linear searches are O(n) and take about the same time. Objects are no slower to search. The big gain comes from choosing a better structure (HashMap), and that is much easier when each vehicle is a single object that can be stored as a value.

**Conclusion:** objects cost a few extra bytes per vehicle, but they keep related data together. Adding an attribute is a one-line change, and objects are needed for polymorphic fees (Tasks 7 and 8). That is why the design uses objects.

## Files

| File | Purpose |
|---|---|
| `RepresentationCompare.java` | Both designs and the search-time measurement |
| `output/RepresentationCompare.txt` / `.png` | Program output |

**Run:** `javac RepresentationCompare.java && java RepresentationCompare`
