# Task 9: Introduce Abstraction (4 marks)

## 1. Which class is abstract, and which methods

**`Vehicle` is abstract.** A plain "vehicle" never enters the campus; it is always a two-wheeler, car, and so on. Making it abstract means `new Vehicle("X")` **does not compile**.

| Method | Abstract? | Reason |
|---|---|---|
| `ratePerHour()` | **abstract** | Every vehicle *must* have a rate, but `Vehicle` cannot know it. The compiler forces each subclass to supply one. |
| `calculateFee(int)` | concrete | The *how* (every started hour, daily cap) is the same for all, so it is written once. In `AbstractionDemo` it is even `final`. |
| `getRegNo()`, `equals`, `hashCode`, `toString` | concrete | Shared behaviour based on shared state. |

This separates **what** the system does (charge a fee for a stay) from **how** each type prices it (its rate).

## 2. Which capabilities are interfaces

| Interface | Implemented by | Why an interface |
|---|---|---|
| `Chargeable` | `ElectricCar`, `ElectricScooter` (demo) | Charging is a **capability** that unrelated classes may have: an EV car and an EV two-wheeler share no parent other than `Vehicle`, and not every `Vehicle` can charge. |
| `Payable` | `Visitor`, `PassHolder` (demo) | "How much does this user pay?" is a behaviour, not a kind of person. |
| `Pass` (Task 12) | `NoPass`, `MonthlyPass`, `OneDayPass` | Each pass changes the payable amount differently; new passes are new classes. |

## 3. Abstract class here, interface there: why

| Choose an **abstract class** when… | Choose an **interface** when… |
|---|---|
| Subclasses share **state** (`regNo`) | There is no shared state, only a contract |
| A **constructor** must validate that state | Objects come from different class families |
| There is shared, partly finished behaviour (the fee algorithm) | A class needs **several** such capabilities |
| There is a true *is-a* relationship (`Car` is a `Vehicle`) | It is a *can-do* relationship (`ElectricCar` can charge) |

`Vehicle` has state and a validating constructor → **abstract class**. `Chargeable` is an optional ability shared across the hierarchy → **interface**.

## 4. Multiple behaviours through interfaces

A Java class can extend only **one** class but implement **many** interfaces:
```java
class ElectricCar extends Car implements Chargeable { ... }
// could also be: implements Chargeable, Comparable<Vehicle>, Serializable
```
`ElectricCar` gets parking behaviour from `Car` and charging behaviour from `Chargeable`. Code that needs only charging can accept a `Chargeable` parameter and work with an `ElectricCar` or an `ElectricScooter` alike, as the demo does with `if (v instanceof Chargeable c)`.

## 5. Where default and static interface methods help

- **Default method:** `Chargeable.chargingReceipt(int kWh)` builds the receipt text from `chargingFee()`. Every chargeable vehicle gets it **without writing it**, and adding it later does not break existing implementations. Output: `charging 10 kWh x Rs 12.0 = Rs 120.0`.
- **Static method:** `Payable.roundToRupee(double)` and `Chargeable.validateKWh(int)` (Task 11) are helpers tied to the interface's purpose that need no object.
- **Constant:** `Chargeable.RATE_PER_KWH = 12` (implicitly `public static final`).

Output (`output/AbstractionDemo.png`):
```
Car TN02B2 parking Rs 90.0 (Visitor)
ElectricCar TN03C3 parking Rs 90.0 (Visitor) | charging 10 kWh x Rs 12.0 = Rs 120.0
ElectricScooter TN04D4 parking Rs 0.0 (PassHolder) | charging 10 kWh x Rs 12.0 = Rs 60.0
Payable.roundToRupee(89.6) = 90.0
```
(The scooter's receipt shows the *standard* rate text from the default method, while its overridden `chargingFee` uses a half-rate slow charger. That shows a default method can call the implementing class's own override.)

---

## Alternative Concept and Complexity Analysis: swapping them

**If `Vehicle` became an interface with default methods:**
```java
interface Vehicle {
    String getRegNo();                       // every class must now store regNo itself
    double ratePerHour();
    default double calculateFee(int m) { return ((m + 59) / 60) * ratePerHour(); }
}
class Car implements Vehicle {
    private final String regNo;              // duplicated in every class
    Car(String r) { /* validation duplicated in every class */ this.regNo = r; }
    ...
}
```
**If `Chargeable` became an abstract class:**
```java
abstract class Chargeable { abstract double chargingFee(int kWh); }
class ElectricCar extends Car, Chargeable   // ✗ does not compile: only one superclass allowed
```

**What can no longer be expressed:**

| Lost when `Vehicle` → interface | Lost when `Chargeable` → abstract class |
|---|---|
| **State:** interfaces have no instance fields, so `regNo` must be re-declared in every class | **Multiple inheritance:** `ElectricCar` already extends `Car`, so it cannot also extend `Chargeable` |
| **Constructor:** no shared validation; each class repeats it, or forgets it | Unrelated classes (car EV, scooter EV) can no longer share the capability |
| `protected` / `private` members: interface methods are public (Java 9+ allows only private helpers) | You would need to move charging into `Vehicle` (then every vehicle "can charge") or duplicate it |
| `final` methods: default methods can always be overridden, so `equals`/`hashCode` cannot be locked | |

**Time per method call:** both use virtual dispatch: `invokevirtual` for a class, `invokeinterface` for an interface. Interface calls need a slightly costlier lookup, but the JIT optimises both to about the same speed when only a few implementing types are seen, so the cost is **O(1)** either way.

**Space per object:** unchanged. Fields live in the object whether they are declared in an abstract class or repeated in each class (`regNo` is one reference either way). Interfaces add no per-object space; their methods live once per class.

**Conclusion:** the swap gains nothing in speed or memory, loses shared state and constructors for `Vehicle`, and makes `ElectricCar` impossible to express. The original choice (abstract `Vehicle`, interface `Chargeable`) is kept.

## Files
- `AbstractionDemo.java`: abstract class, two interfaces, default and static methods, multiple behaviours
- `output/AbstractionDemo.txt` / `.png`

**Run:** `javac AbstractionDemo.java && java AbstractionDemo`
