# Task 10: Enforce Encapsulation through Packages and Access Modifiers (3 marks)

## 1. Package structure

The full system (`Task11/src`, `Task12/src`) and this task's demo use the same layout:

```
src/
└── parking/
    ├── model/      ← domain objects: what the system is about
    │   ├── Vehicle, TwoWheeler, Car, ElectricCar, Chargeable
    │   ├── ParkingTicket, UserCategory
    │   └── (Task 10 demo) ParkingSlot, Stay
    ├── service/    ← business processes that use the model
    │   ├── ParkingFacility, ParkingService, VehicleFactory
    │   └── (Task 10 demo) FeeService
    └── app/        ← entry points: input/output only
        └── ParkingApp, ParkingTests, EncapsulationDemo
```
- **Dependencies point one way:** `app → service → model`. The model never imports the service or app.
- A class's package is its folder, and the `package parking.model;` line must match it.
- Each class can be used from another package only through its `public` members, which form the package's "public API".

## 2. Access modifier for each field and method

| Member | Modifier | Why |
|---|---|---|
| All instance fields (`regNo`, `floor`, `minutes`, `slots`, `active`, `totalCollection`…) | `private` | Only the owning class may change its state, so every change passes its checks. |
| Constants (`MAX_FLOORS`, `RATE_PER_KWH`, `FLAT_FEE`) | `public static final` | Safe to expose: they cannot change. |
| `FeeService.CAR_RATE` | `private static final` | An internal detail. Other classes ask for the fee, not the rate. |
| `Vehicle(String)` constructor | `protected` | Only subclasses may call it; nobody does `new Vehicle(...)`. |
| `ratePerHour()`, `dailyMaximum()` | `protected` | Subclasses must override them; outside code has no reason to call them. |
| `Stay.isOvernight()` | `protected` | For future subclasses (e.g. `OvernightStay`). |
| `Stay.startedHours()` | *package-private* (no modifier) | A helper for `parking.model` only. `FeeService` in `parking.service` **cannot** call it: the line is shown commented out because it would not compile. |
| Getters, `calculateFee`, `enter`, `exit`, `allocate`, `release` | `public` | The operations other packages are meant to use. |
| `getRegNo`, `equals`, `hashCode` | `public final` | Public, but no subclass may change them. |
| `VehicleFactory()` constructor | `private` | Utility class: no objects. |

## 3. Getters and setters that validate

There are **no setters for fields that must never change** (`regNo`, `floor`, `slot`, `ticketNo`): they are `final` and set once in the constructor after validation. Where change is allowed, the method checks the business rule first:

| Method | Rejects |
|---|---|
| `Stay.setMinutes(int)` | Negative durations; stays longer than 7 days |
| `Stay.setKWhCharged(Integer)` | Negative kWh |
| `ParkingSlot(floor, number, slotsOnFloor)` | Floor outside 0–4; slot number outside the floor's size |
| `ParkingSlot.occupy(regNo)` | Blank registration; a slot that already holds a vehicle |
| `ParkingSlot.free()` | Freeing a slot that is already free |
| `ParkingTicket.close(exit, fee)` (Task 11) | Closing twice; missing exit time; negative fee |
| `Vehicle(regNo)` (Task 11) | Registration not matching `[A-Z]{2}[0-9A-Z]{2,8}` |

If a check fails, an exception is thrown **before** any field changes, so the object stays valid. Output (`output/EncapsulationDemo.png`):
```
REJECTED setMinutes(-15) -> Duration cannot be negative: -15
   stay still holds 130 min (state protected)
REJECTED new ParkingSlot(7, 0, 20) -> Invalid floor 7 (0-4)
REJECTED occupy an occupied slot -> Slot F0-S3 already holds TN01AB1111
```

## 4. Wrapper classes and autoboxing: the risks

- `Stay.kWhCharged` is an `Integer`, **not** `int`, so it can be `null` = "did not charge", which is different from charging 0 kWh. Collections (`Map<String, ParkingTicket>`, `List<...>`) also need objects, so `int` values are autoboxed when stored in them.
- **Risk 1: comparing with `==`.** `Integer` values from −128 to 127 are cached, so `==` happens to work for them. Larger values are different objects:
  ```
  Integer 127 == 127   : true   (cached objects)
  Integer 1000 == 1000 : false  (different objects!)
  c.equals(d)          : true   (compare values with equals)
  ```
  A ticket-number check like `t1.getNo() == t2.getNo()` on `Integer`s would pass in testing (small numbers) and fail in production (ticket 1001+). **The design keeps ticket numbers as `int`** and uses `.equals()` whenever wrappers are compared.
- **Risk 2: unboxing `null`.** `int kWh = stay.getKWhCharged();` throws `NullPointerException` when nothing was charged. The demo shows this, so callers must check for `null` first.
- **Risk 3: hidden cost.** Autoboxing in a tight loop creates objects. The system uses primitives for arithmetic (minutes, hours, fees).

---

## Alternative Concept and Complexity Analysis: public fields + caller-side validation

**Alternative:**
```java
class PublicStay { public int minutes; }

// every caller must remember:
if (stay.minutes < 0 || stay.minutes > 10080) throw ...;
stay.minutes = value;
```

**What could go wrong:** the demo shows `p.minutes = -15` is accepted, and the fee becomes `((-15 + 59) / 60) * 30` = **Rs 0**: silently wrong, no error. Any class in any package can do this. Also:
- A rule change (max stay 7 → 3 days) must be found and edited in **every** caller. Miss one and the rule is enforced in some places only.
- Fields cannot be made read-only, so a slot's floor number could be reassigned while a car is parked.
- Internal representation is frozen: changing `minutes` to `Duration` breaks every caller.

| | Private fields + validating setter | Public fields + caller validation |
|---|---|---|
| Time cost of validation per update | **O(1)** (a couple of comparisons) | **O(1)** if the caller remembers; **0** if it forgets (that is the bug) |
| Places the same check must be written | **1** (the setter) | **p**: one per place that updates the field (ParkingApp, ParkingService, tests, any future gate code…) |
| Guarantee | Object is always valid | Only as good as the most careless caller |
| Cost of changing a rule | Edit 1 method | Edit p call sites |

The run-time cost is identical, O(1) per update; the JIT usually inlines a getter or setter. The difference is **where** the check lives: one place vs p places. Encapsulation is kept.

## Files
| File | Purpose |
|---|---|
| `src/parking/model/ParkingSlot.java` | Validated floor/slot position, occupy/free rules |
| `src/parking/model/Stay.java` | Validated duration; `Integer` kWh; package-private and protected members |
| `src/parking/service/FeeService.java` | Uses only the model's public API |
| `src/parking/app/EncapsulationDemo.java` | Rejections, public-field alternative, wrapper pitfalls |
| `output/` | Captured output and screenshot |

**Run:**
```bash
javac -d out $(find src -name "*.java")
java -cp out parking.app.EncapsulationDemo
```
