# Task 8: Apply Polymorphism (4 marks)

## 1. Compile-time polymorphism: overloading

```java
public double calculateFee(int minutes) { ... }                       // overload 1
public double calculateFee(LocalTime in, LocalTime out) { ... }       // overload 2 → converts, calls overload 1
```
The **compiler** chooses the overload from the **number and types of the arguments** at the call site:
- `car.calculateFee(130)` → one `int` argument → overload 1.
- `car.calculateFee(LocalTime.of(9,0), LocalTime.of(11,10))` → two `LocalTime` arguments → overload 2.

The choice is fixed in the bytecode before the program runs. The return type plays no part in choosing. Output (`output/PolymorphismDemo.png`):
```
    -> compiler chose calculateFee(int)
    -> runtime executed PCar.ratePerHour()
  car.calculateFee(130) = Rs 90.0
    -> compiler chose calculateFee(LocalTime, LocalTime)
    -> compiler chose calculateFee(int)
```
Other overloads in the project: `ParkingTicket`'s constructors (Task 4), and `ParkingFacility(String name, int... slotsPerFloor)`.

## 2. One array of `Vehicle` references (upcasting)

```java
Vehicle[] parked = { new TwoWheeler("TN01A1"), new Car("TN02B2"), new ElectricCar("TN03C3") };
```
Each `new` creates a subclass object, and the reference is **upcast** automatically to `Vehicle`. That is always safe, because every `TwoWheeler` *is a* `Vehicle`. The array stores only references, so objects of different sizes fit in one array. Through a `Vehicle` reference you can call only methods declared in `Vehicle`. To reach `chargingFee`, the code checks `v instanceof Chargeable c`.

## 3. Dynamic method dispatch

At run time, the JVM looks at the **actual object** (not the declared type) and runs the most specific override, using the class's virtual method table.

### Trace: `v.calculateFee(130)` when `v = parked[2]`

| Step | What happens |
|---|---|
| Declared type of `v` | `Vehicle` |
| Actual object type | `ElectricCar` |
| Compile time | Compiler finds `calculateFee(int)` in `Vehicle` → emits `invokevirtual Vehicle.calculateFee(I)D` |
| Run time, 1 | JVM looks up `calculateFee(int)` in `ElectricCar`'s table: not overridden → inherited from `Vehicle` → runs `Vehicle.calculateFee` |
| Run time, 2 | Inside, `ratePerHour()` is called on `this` (the `ElectricCar`). Lookup: `ElectricCar` doesn't override it → `Car.ratePerHour()` → **30** |
| Result | hours = (130 + 59) / 60 = 3 → 3 × 30 = **Rs 90.0** |
| Then | `v instanceof Chargeable c` is true → `c.chargingFee(10)` → `ElectricCar.chargingFee` → 10 × 12 = 120 → total **Rs 210.0** |

`PolymorphismDemo` prints this live for all three objects: the same line `v.calculateFee(130)` runs `PTwoWheeler.ratePerHour()`, `PCar.ratePerHour()` and `PElectricCar.ratePerHour()` in turn.

## 4. Why this avoids long if–else chains

The loop in `FeeDemo` never asks "what type is this?". It calls `v.calculateFee(130)`, and each object answers for itself. Adding a `DeliveryVan` (Task 12) does not change this loop at all. With if–else, every place that computes a fee would need a new branch, and the order of the branches matters (see below).

---

## Complete the Code: `FeeDemo.java`

- **TODO 1:** `int hours = (minutes + 59) / 60; return hours * ratePerHour();` (integer ceiling, no casting).
- **TODO 2:** `Car(String regNo) { super(regNo); }` and `@Override protected double ratePerHour() { return 30; }`
- **TODO 3:** `@Override public double chargingFee(int kWh) { return kWh * 12; }`

**Output (matches the expected output exactly):**
```
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```
Screenshot: `output/FeeDemo.png`

### Why the last line prints `true`, and is it correct?

`new Car("TN02B2").equals(new ElectricCar("TN02B2"))` calls `Vehicle.equals`, which returns true when `o instanceof Vehicle` and the registration numbers match. An `ElectricCar` *is a* `Vehicle`, and both have "TN02B2", so the result is **true**, even though one is a `Car` and the other an `ElectricCar`.

**Decision: this is the correct behaviour for this system.** A registration number identifies **one physical vehicle**. The business rule is "a registration number can have only one active ticket". If the gate reads TN02B2 as a `Car` and someone tries to enter TN02B2 as an `ElectricCar`, the system **must** treat it as the same vehicle and reject the duplicate, which `HashMap`/`HashSet` will do only because `equals` says true. Using `getClass() == o.getClass()` instead would let one number park twice with two types.

To keep this safe, `equals` and `hashCode` are made **`final`** in the full system (Task 11), so no subclass can change the rule and break symmetry (`a.equals(b)` but `!b.equals(a)`). The type mismatch itself is a data-entry error, reported separately.

---

## Alternative Concept and Complexity Analysis: dynamic dispatch vs `instanceof` + casting

```java
static double feeByInstanceof(Object v, int minutes) {
    int hours = (minutes + 59) / 60;
    if (v instanceof PElectricCar) return hours * 25;   // must test the MOST specific type first
    else if (v instanceof PCar) return hours * 30;
    else if (v instanceof PTwoWheeler) return hours * 10;
    throw new IllegalArgumentException("Unknown vehicle");
}
```
It gives the same answers (`30.0, 90.0, 75.0`), but:

| | Dynamic dispatch | `instanceof` chain |
|---|---|---|
| Time per call, k types | **O(1)**: one vtable lookup | **O(k)** worst case: up to k checks |
| Code size | One override per class (1–3 lines) | One branch per type **in every method that varies**: fee, rate, receipt… so k × m branches |
| Adding a type | New subclass; no existing code touched | Edit every chain. **Order matters:** if `PCar` is tested before `PElectricCar`, EVs are silently billed as cars. |
| Errors | Compiler forces abstract methods to be implemented | A forgotten branch is a run-time exception |

**Conclusion:** dynamic dispatch is O(1) and keeps each rule in its own class. The `instanceof` chain is O(k), grows with every type, and has the subtle ordering bug. The only `instanceof` kept in the design is for an optional *capability* (`Chargeable`), which is the right use of it.

## Files
| File | Purpose |
|---|---|
| `FeeDemo.java` | Complete the Code (Tasks 6–9 hierarchy), TODOs 1–3 completed |
| `PolymorphismDemo.java` | Overloading, upcasting, printed dispatch trace, instanceof alternative |
| `output/` | Captured outputs and screenshots |

**Run:** `javac *.java && java FeeDemo && java PolymorphismDemo`
