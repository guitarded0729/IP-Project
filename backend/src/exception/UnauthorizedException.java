package exception;

// OOP CONCEPT: Exception Handling
// Custom checked exception thrown when an unauthenticated or unauthorized action is attempted
public class UnauthorizedException extends Exception {
    
    // OOP CONCEPT: Parameterized Constructor
    public UnauthorizedException(String message) {
        super(message);
    }
}
