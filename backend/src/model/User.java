package model;

import exception.InvalidInputException;
import util.JsonUtil;

// OOP CONCEPT: Abstraction
// User is an abstract base class representing any person interacting with the campus system
public abstract class User {
    
    // OOP CONCEPT: Encapsulation (private fields)
    private String idNumber;
    private String name;
    private String contact;

    // OOP CONCEPT: Default Constructor
    public User() {
        this.idNumber = "";
        this.name = "";
        this.contact = "";
    }

    // OOP CONCEPT: Parameterized Constructor using 'this' keyword
    public User(String idNumber, String name, String contact) throws InvalidInputException {
        setIdNumber(idNumber);
        setName(name);
        setContact(contact);
    }

    // OOP CONCEPT: Abstract Method (must be implemented by child classes)
    public abstract String getRole();

    // OOP CONCEPT: Abstract Method (polymorphic permission check)
    public abstract boolean canManageClaims();

    // OOP CONCEPT: Encapsulation (Getters & Setters with validation)
    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) throws InvalidInputException {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new InvalidInputException("ID Number cannot be empty.");
        }
        this.idNumber = idNumber.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) throws InvalidInputException {
        if (contact == null || contact.trim().isEmpty()) {
            throw new InvalidInputException("Contact info cannot be empty.");
        }
        this.contact = contact.trim();
    }

    // OOP CONCEPT: Polymorphism (overridden in child classes if needed)
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"idNumber\":").append(JsonUtil.toJsonString(this.idNumber)).append(",");
        sb.append("\"name\":").append(JsonUtil.toJsonString(this.name)).append(",");
        sb.append("\"contact\":").append(JsonUtil.toJsonString(this.contact)).append(",");
        sb.append("\"role\":").append(JsonUtil.toJsonString(getRole()));
        sb.append("}");
        return sb.toString();
    }
}
