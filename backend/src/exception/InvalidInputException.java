package exception;

// OOP CONCEPT: Exception Handling
// Custom checked exception thrown when client input fails business or validation rules
public class InvalidInputException extends Exception {
    
    // OOP CONCEPT: Constructor Overloading / super keyword
    public InvalidInputException(String message) {
        super(message);
    }
}
