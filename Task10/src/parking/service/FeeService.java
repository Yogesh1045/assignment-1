package parking.service;

import parking.model.Stay;

/** Business logic lives in the service package; it only sees the public API of the model. */
public class FeeService {
    private static final int CAR_RATE = 30;           // private: an implementation detail

    public int carFee(Stay stay) {
        // stay.startedHours();   <- would not compile: package-private in parking.model
        return stay.billableHours() * CAR_RATE;
    }
}
