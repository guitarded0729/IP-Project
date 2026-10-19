package model;

import exception.InvalidInputException;

// OOP CONCEPT: Inheritance (Staff extends User)
public class Staff extends User {

    // OOP CONCEPT: Default Constructor
    public Staff() {
        super();
    }

    // OOP CONCEPT: Parameterized Constructor using 'super'
    public Staff(String idNumber, String name, String contact) throws InvalidInputException {
        super(idNumber, name, contact);
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String getRole() {
        return "STAFF";
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public boolean canManageClaims() {
        return false;
    }
}
