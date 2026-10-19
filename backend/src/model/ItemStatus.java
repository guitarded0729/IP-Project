package model;

// OOP CONCEPT: Enums
// Represents the current lifecycle status of an item
public enum ItemStatus {
    OPEN("Open"),
    CLAIM_PENDING("Claim Pending"),
    RETURNED("Returned");

    // OOP CONCEPT: Encapsulation
    private final String displayName;

    // OOP CONCEPT: Constructor in Enum
    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    // Helper method to safely parse strings into ItemStatus
    public static ItemStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OPEN;
        }
        for (ItemStatus status : ItemStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim()) || 
                status.displayName.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return OPEN;
    }
}
