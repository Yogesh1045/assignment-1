package parking.model;

public enum UserCategory {
    STUDENT, STAFF, VISITOR;

    /** Only students and staff may hold a monthly pass. */
    public boolean canHoldMonthlyPass() { return this != VISITOR; }
}
