package model;

// OOP CONCEPT: Enums
// Categorizes lost and found items on campus
public enum Category {
    ELECTRONICS("Electronics"),
    BOOKS("Books & Stationery"),
    CLOTHING("Clothing"),
    ID_CARDS("ID Cards & Documents"),
    KEYS("Keys"),
    BAGS("Bags & Wallets"),
    ACCESSORIES("Accessories"),
    OTHER("Other");

    // OOP CONCEPT: Encapsulation
    private final String displayName;

    // OOP CONCEPT: Constructor in Enum
    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    // Helper method to safely match category from user input
    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        String clean = text.trim();
        for (Category cat : Category.values()) {
            if (cat.name().equalsIgnoreCase(clean) || 
                cat.displayName.equalsIgnoreCase(clean)) {
                return cat;
            }
        }
        return OTHER;
    }
}
