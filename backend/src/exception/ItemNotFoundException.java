package exception;

// OOP CONCEPT: Exception Handling
// Custom checked exception thrown when an item is not found by ID
public class ItemNotFoundException extends Exception {
    
    // OOP CONCEPT: Parameterized Constructor
    public ItemNotFoundException(String message) {
        super(message);
    }
}
