package model;

import exception.InvalidInputException;
import util.JsonUtil;

// OOP CONCEPT: Abstraction
// Item is an abstract base class that encapsulates common fields and behaviors of lost and found records
public abstract class Item {

    // OOP CONCEPT: static keyword (shared across all Item instances)
    private static int idCounter = 1;

    // OOP CONCEPT: Encapsulation (private fields)
    private int id;
    private String title;
    private String description;
    private Category category;
    private String location;
    private String date; // Format: YYYY-MM-DD
    private ItemStatus status;

    // OOP CONCEPT: Composition (Item "has-a" User as reporter)
    private User reporter;

    // Static helper to generate next ID
    public static synchronized int generateNextId() {
        return idCounter++;
    }

    // Static helper to update ID counter when loading from persistent storage
    public static synchronized void updateIdCounter(int maxIdSeen) {
        if (maxIdSeen >= idCounter) {
            idCounter = maxIdSeen + 1;
        }
    }

    // OOP CONCEPT: Default Constructor
    public Item() {
        this.id = generateNextId();
        this.title = "";
        this.description = "";
        this.category = Category.OTHER;
        this.location = "Other";
        this.date = "";
        this.status = ItemStatus.OPEN;
        this.reporter = null;
    }

    // OOP CONCEPT: Parameterized Constructor using 'this' keyword
    public Item(int id, String title, String description, Category category, 
                String location, String date, ItemStatus status, User reporter) throws InvalidInputException {
        this.id = id;
        setTitle(title);
        setDescription(description);
        setCategory(category);
        setLocation(location);
        setDate(date);
        setStatus(status);
        setReporter(reporter);
    }

    // OOP CONCEPT: Abstract Method (Child classes define their specific type: LOST or FOUND)
    public abstract String getType();

    // OOP CONCEPT: Abstract Method for Polymorphism (Child classes provide specific summary)
    public abstract String getSummary();

    // OOP CONCEPT: Encapsulation (Getters and Setters with validation)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) throws InvalidInputException {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidInputException("Item title cannot be empty.");
        }
        if (title.trim().length() < 2) {
            throw new InvalidInputException("Item title must be at least 2 characters.");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) throws InvalidInputException {
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidInputException("Item description cannot be empty.");
        }
        this.description = description.trim();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) throws InvalidInputException {
        if (category == null) {
            throw new InvalidInputException("Item category cannot be null.");
        }
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) throws InvalidInputException {
        if (location == null || location.trim().isEmpty()) {
            throw new InvalidInputException("Item location cannot be empty.");
        }
        this.location = location.trim();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) throws InvalidInputException {
        if (date == null || date.trim().isEmpty()) {
            throw new InvalidInputException("Date cannot be empty.");
        }
        this.date = date.trim();
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) throws InvalidInputException {
        if (status == null) {
            throw new InvalidInputException("Item status cannot be null.");
        }
        this.status = status;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) throws InvalidInputException {
        if (reporter == null) {
            throw new InvalidInputException("Reporter user details cannot be null.");
        }
        this.reporter = reporter;
    }

    // OOP CONCEPT: Polymorphism (method will be overridden by child classes)
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(this.id).append(",");
        sb.append("\"type\":").append(JsonUtil.toJsonString(getType())).append(",");
        sb.append("\"title\":").append(JsonUtil.toJsonString(this.title)).append(",");
        sb.append("\"description\":").append(JsonUtil.toJsonString(this.description)).append(",");
        sb.append("\"category\":").append(JsonUtil.toJsonString(this.category.name())).append(",");
        sb.append("\"categoryDisplay\":").append(JsonUtil.toJsonString(this.category.getDisplayName())).append(",");
        sb.append("\"location\":").append(JsonUtil.toJsonString(this.location)).append(",");
        sb.append("\"date\":").append(JsonUtil.toJsonString(this.date)).append(",");
        sb.append("\"status\":").append(JsonUtil.toJsonString(this.status.name())).append(",");
        sb.append("\"statusDisplay\":").append(JsonUtil.toJsonString(this.status.getDisplayName())).append(",");
        sb.append("\"summary\":").append(JsonUtil.toJsonString(getSummary())).append(",");
        sb.append("\"reporter\":").append(this.reporter != null ? this.reporter.toJson() : "null");
        sb.append("}");
        return sb.toString();
    }
}
