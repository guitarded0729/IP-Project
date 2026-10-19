package model;

import exception.InvalidInputException;

// OOP CONCEPT: Inheritance (Student extends User)
public class Student extends User {

    // OOP CONCEPT: Default Constructor
    public Student() {
        super();
    }

    // OOP CONCEPT: Parameterized Constructor using 'super'
    public Student(String idNumber, String name, String contact) throws InvalidInputException {
        super(idNumber, name, contact);
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String getRole() {
        return "STUDENT";
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public boolean canManageClaims() {
        return false;
    }
}
