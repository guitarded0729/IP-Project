package service;

import exception.InvalidInputException;
import exception.ItemNotFoundException;
import model.Category;
import model.Item;
import java.util.ArrayList;

// OOP CONCEPT: Abstraction (Interface defining the contract for item management)
public interface ItemOperations {

    // Add a new item to storage
    void add(Item item) throws InvalidInputException;

    // Retrieve all items
    // OOP CONCEPT: Parent-type reference holding child objects (Item holds LostItem / FoundItem)
    ArrayList<Item> getAll();

    // Retrieve single item by ID
    Item getById(int id) throws ItemNotFoundException;

    // Update existing item
    void update(Item item) throws ItemNotFoundException, InvalidInputException;

    // Delete item by ID
    void delete(int id) throws ItemNotFoundException;

    // OOP CONCEPT: Polymorphism (Method Overloading - Version 1: search by keyword only)
    ArrayList<Item> search(String keyword);

    // OOP CONCEPT: Polymorphism (Method Overloading - Version 2: search by keyword and category)
    ArrayList<Item> search(String keyword, Category category);
}
