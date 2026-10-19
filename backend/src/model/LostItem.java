package model;

import exception.InvalidInputException;
import util.JsonUtil;

// OOP CONCEPT: Inheritance (LostItem extends abstract class Item)
public class LostItem extends Item {

    // OOP CONCEPT: Default Constructor calling super()
    public LostItem() {
        super();
    }

    // OOP CONCEPT: Parameterized Constructor using 'super'
    public LostItem(int id, String title, String description, Category category,
                    String location, String date, ItemStatus status, User reporter) throws InvalidInputException {
        super(id, title, description, category, location, date, status, reporter);
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String getType() {
        return "LOST";
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String getSummary() {
        return "LOST: " + getTitle() + " (reported at " + getLocation() + " on " + getDate() + ")";
    }

    // OOP CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(getId()).append(",");
        sb.append("\"type\":").append(JsonUtil.toJsonString(getType())).append(",");
        sb.append("\"title\":").append(JsonUtil.toJsonString(getTitle())).append(",");
        sb.append("\"description\":").append(JsonUtil.toJsonString(getDescription())).append(",");
        sb.append("\"category\":").append(JsonUtil.toJsonString(getCategory().name())).append(",");
        sb.append("\"categoryDisplay\":").append(JsonUtil.toJsonString(getCategory().getDisplayName())).append(",");
        sb.append("\"location\":").append(JsonUtil.toJsonString(getLocation())).append(",");
        sb.append("\"date\":").append(JsonUtil.toJsonString(getDate())).append(",");
        sb.append("\"status\":").append(JsonUtil.toJsonString(getStatus().name())).append(",");
        sb.append("\"statusDisplay\":").append(JsonUtil.toJsonString(getStatus().getDisplayName())).append(",");
        sb.append("\"summary\":").append(JsonUtil.toJsonString(getSummary())).append(",");
        sb.append("\"isClaimable\":false,");
        sb.append("\"reporter\":").append(getReporter() != null ? getReporter().toJson() : "null");
        sb.append("}");
        return sb.toString();
    }
}
