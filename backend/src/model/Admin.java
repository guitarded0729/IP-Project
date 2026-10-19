package model;

import exception.InvalidInputException;

// OOP CONCEPT: Inheritance (Admin extends User)
public class Admin extends User {

    // OOP CONCEPT: static and final constants
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "password123";

    // OOP CONCEPT: Default Constructor
    public Admin() {
        super();
    }

    // OOP CONCEPT: Parameterized Constructor using 'super'
    public Admin(String idNumber, String name, String contact) throws InvalidInputException {
        super(idNumber, name, contact);
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String getRole() {
        return "ADMIN";
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public boolean canManageClaims() {
        return true;
    }
}
