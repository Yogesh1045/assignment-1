# Task 7: Override and Specialise Behaviour (4 marks)

## 1. Which methods are overridden, and why

| Method | Overridden in | Why the subclass version is needed |
|---|---|---|
| `ratePerHour()` (abstract in `Vehicle`) | `TwoWheeler` (10), `Car` (30) | The fee *algorithm* is the same for everyone; only the rate differs. Overriding one small method keeps the algorithm in one place. |
| `calculateFee(int)` | `ElectricCar` in `OverrideDemo` (first 30 min free); `EBike` in Task 12 (flat fee) | When the *rule itself* differs, not only the rate. `ElectricCar` reuses the parent rule with `super.calculateFee(...)`. |
| `calculateFee(LocalTime, LocalTime)` | `DeliveryVan` (Task 12) | The van's price depends on *when* it parks (peak 9–5), which the minutes-only version cannot know. |
| `chargingFee(int)` | `ElectricCar` (from `Chargeable`) | Only chargeable vehicles have it. |
| `toString()`, `equals()`, `hashCode()` | `Vehicle` | So that vehicles print readably and are compared by registration number (section 4). |

(In `OverrideDemo` the "EV first 30 minutes free" rule only demonstrates specialising with `super`. The main system keeps the assignment's rule: EV parking = car rate.)

## 2. `@Override` and the rules of overriding

`@Override` asks the compiler to check that the method really overrides something. If I write `ratePerhour()` (lower-case h) or `calculateFee(long)` by mistake, **compilation fails** instead of silently creating a new, unused method.

Rules (all followed in the code):
- **Same signature:** same name and parameter types (`calculateFee(int)`).
- **Same or covariant return type:** `double` → `double`.
- **Access cannot be narrower:** `protected ratePerHour()` in `Vehicle` may become `protected` or `public` in the subclass, but never `private` or package-private.
- **Exceptions:** an override may not throw new or broader *checked* exceptions.
- `static`, `private` and `final` methods cannot be overridden.

## 3. Where `final` is used and why

| `final` on | Example | Reason |
|---|---|---|
| Variable (field) | `private final String regNo` | A vehicle's registration never changes; also required for a stable `hashCode()`. |
| Constant | `public static final int MINUTES_PER_HOUR = 60` | Shared constant that must never change. |
| Method | `public final String getRegNo()`, `final equals/hashCode` (Task 11) | No subclass may change identity rules; otherwise `Car` and `ElectricCar` could disagree about equality and break `HashMap`. |
| Class | `final class ElectricCar` | No further subclass is planned. It is easy to remove `final` later if needed. Value classes like `VehicleType` and `NoPass` are `final` too. |

`final` is **not** put on `calculateFee` in `Vehicle`, because Task 12 subclasses (`EBike`, `DeliveryVan`) must be able to specialise it.

## 4. `toString()`, `equals()` and `hashCode()`

```java
@Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Vehicle)) return false;
    return regNo.equals(((Vehicle) o).regNo);
}
@Override public int hashCode() { return regNo.hashCode(); }
@Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
```
- Two `Vehicle` objects with the same registration number are **equal**. One physical vehicle is one vehicle, whichever object represents it.
- `hashCode()` **must** be overridden together with `equals()`: equal objects must have equal hash codes, or `HashSet`/`HashMap` put them in different buckets. `OverrideDemo` proves it: `BrokenVehicle` overrides only `equals`, and the set ends up holding **2 "equal" vehicles**. With both overridden, the set **rejects the duplicate**.
- `toString()` gives readable logs, like `Car[TN02B2]`, instead of `Car@1b6d3586`.

Output (`output/OverrideDemo.png`):
```
a == b          : false        (two objects)
a.equals(b)     : true         (same registration)
same hashCode   : true
HashSet rejects duplicate regNo: true
Without hashCode the set holds : 2 'equal' vehicles (bug)
```

---

## Alternative Concept and Complexity Analysis: overriding vs a single `switch`

**Switch-based alternative:**
```java
static double feeBySwitch(String type, int minutes) {
    int hours = (minutes + 59) / 60;
    switch (type) {
        case "TWO_WHEELER": return hours * 10;
        case "CAR":         return hours * 30;
        case "EV":          return ((Math.max(0, minutes - 30) + 59) / 60) * 30;
        default: throw new IllegalArgumentException("Unknown type " + type);
    }
}
```

| | Overriding (dynamic dispatch) | `switch` on a type field |
|---|---|---|
| Time per fee calculation, k types | **O(1)**: one virtual-table lookup | String switch: hash of the string, then a jump, so O(1) average. An if–else chain is **O(k)**. |
| Classes to change when a type is added | **0 existing classes**; write 1 new subclass | Every method that switches on the type (fee, rate, receipt, report…). Miss one and it fails only at run time with `default`. |
| Compiler help | Abstract method forces every subclass to supply a rate | None: a missing `case` still compiles |
| Where the rules live | Each class holds its own rule | One method holds all rules (easier to see, harder to extend) |

**My measurement** (5,000,000 calls): overriding ≈ 9.5 ns, switch ≈ 7.5 ns per call. Both are O(1) and the difference is a few nanoseconds. Both versions give the same fees (30.0, 90.0, 60.0). The real cost of the switch shows up in **maintenance**, not speed: every new type means editing tested code. That is why overriding is used.

## Files
- `OverrideDemo.java`: overriding, `final`, equals/hashCode/toString, broken-hashCode demo, switch alternative and timing
- `output/OverrideDemo.txt` / `.png`

**Run:** `javac OverrideDemo.java && java OverrideDemo`
