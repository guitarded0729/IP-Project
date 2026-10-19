package service;

import exception.UnauthorizedException;
import model.Admin;

import java.util.HashMap;
import java.util.UUID;

// Service handling admin authentication and session token management
public class AuthService {

    // OOP CONCEPT: Collections (HashMap storing active session tokens to login timestamps)
    private final HashMap<String, Long> activeSessions;

    // Session duration: 12 hours in milliseconds
    private static final long SESSION_TIMEOUT_MS = 12 * 60 * 60 * 1000L;

    public AuthService() {
        this.activeSessions = new HashMap<String, Long>();
    }

    // Authenticates admin credentials stored as constants in Admin class
    public synchronized String login(String username, String password) throws UnauthorizedException {
        if (username == null || password == null) {
            throw new UnauthorizedException("Username and password are required.");
        }

        if (Admin.DEFAULT_ADMIN_USERNAME.equals(username.trim()) && 
            Admin.DEFAULT_ADMIN_PASSWORD.equals(password.trim())) {
            
            String token = "adm-" + UUID.randomUUID().toString();
            activeSessions.put(token, System.currentTimeMillis());
            return token;
        }

        throw new UnauthorizedException("Invalid username or password.");
    }

    // Validates whether the provided token is active and unexpired
    public synchronized boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        Long loginTime = activeSessions.get(token.trim());
        if (loginTime == null) {
            return false;
        }
        // Check timeout
        if (System.currentTimeMillis() - loginTime > SESSION_TIMEOUT_MS) {
            activeSessions.remove(token.trim());
            return false;
        }
        return true;
    }

    public synchronized void logout(String token) {
        if (token != null) {
            activeSessions.remove(token.trim());
        }
    }
}
