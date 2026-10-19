package model;

import exception.InvalidInputException;
import util.JsonUtil;

// OOP CONCEPT: Composition / Reference
// Represents a formal claim submitted by a user against a found item
public class Claim {

    // OOP CONCEPT: static keyword
    private static int claimIdCounter = 1;

    // OOP CONCEPT: Encapsulation (private fields)
    private int id;
    private int itemId; // References an Item (Composition / Association)
    private String claimantName;
    private String claimantIdNumber;
    private String claimantContact;
    private String proofDetails;
    private String claimDate;
    private String status; // "PENDING", "APPROVED", "REJECTED"

    public static synchronized int generateNextId() {
        return claimIdCounter++;
    }

    public static synchronized void updateClaimIdCounter(int maxIdSeen) {
        if (maxIdSeen >= claimIdCounter) {
            claimIdCounter = maxIdSeen + 1;
        }
    }

    // OOP CONCEPT: Default Constructor
    public Claim() {
        this.id = generateNextId();
        this.itemId = 0;
        this.claimantName = "";
        this.claimantIdNumber = "";
        this.claimantContact = "";
        this.proofDetails = "";
        this.claimDate = "";
        this.status = "PENDING";
    }

    // OOP CONCEPT: Parameterized Constructor using 'this' keyword
    public Claim(int id, int itemId, String claimantName, String claimantIdNumber,
                 String claimantContact, String proofDetails, String claimDate, String status) throws InvalidInputException {
        this.id = id;
        setItemId(itemId);
        setClaimantName(claimantName);
        setClaimantIdNumber(claimantIdNumber);
        setClaimantContact(claimantContact);
        setProofDetails(proofDetails);
        setClaimDate(claimDate);
        setStatus(status);
    }

    // OOP CONCEPT: Encapsulation (Getters and Setters with validation)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) throws InvalidInputException {
        if (itemId <= 0) {
            throw new InvalidInputException("Valid Item ID is required for a claim.");
        }
        this.itemId = itemId;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) throws InvalidInputException {
        if (claimantName == null || claimantName.trim().isEmpty()) {
            throw new InvalidInputException("Claimant name cannot be empty.");
        }
        this.claimantName = claimantName.trim();
    }

    public String getClaimantIdNumber() {
        return claimantIdNumber;
    }

    public void setClaimantIdNumber(String claimantIdNumber) throws InvalidInputException {
        if (claimantIdNumber == null || claimantIdNumber.trim().isEmpty()) {
            throw new InvalidInputException("Claimant ID number cannot be empty.");
        }
        this.claimantIdNumber = claimantIdNumber.trim();
    }

    public String getClaimantContact() {
        return claimantContact;
    }

    public void setClaimantContact(String claimantContact) throws InvalidInputException {
        if (claimantContact == null || claimantContact.trim().isEmpty()) {
            throw new InvalidInputException("Claimant contact cannot be empty.");
        }
        this.claimantContact = claimantContact.trim();
    }

    public String getProofDetails() {
        return proofDetails;
    }

    public void setProofDetails(String proofDetails) throws InvalidInputException {
        if (proofDetails == null || proofDetails.trim().isEmpty()) {
            throw new InvalidInputException("Proof of ownership details cannot be empty.");
        }
        this.proofDetails = proofDetails.trim();
    }

    public String getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(String claimDate) throws InvalidInputException {
        if (claimDate == null || claimDate.trim().isEmpty()) {
            throw new InvalidInputException("Claim date cannot be empty.");
        }
        this.claimDate = claimDate.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) throws InvalidInputException {
        if (status == null || (!status.equals("PENDING") && !status.equals("APPROVED") && !status.equals("REJECTED"))) {
            throw new InvalidInputException("Claim status must be PENDING, APPROVED, or REJECTED.");
        }
        this.status = status;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(this.id).append(",");
        sb.append("\"itemId\":").append(this.itemId).append(",");
        sb.append("\"claimantName\":").append(JsonUtil.toJsonString(this.claimantName)).append(",");
        sb.append("\"claimantIdNumber\":").append(JsonUtil.toJsonString(this.claimantIdNumber)).append(",");
        sb.append("\"claimantContact\":").append(JsonUtil.toJsonString(this.claimantContact)).append(",");
        sb.append("\"proofDetails\":").append(JsonUtil.toJsonString(this.proofDetails)).append(",");
        sb.append("\"claimDate\":").append(JsonUtil.toJsonString(this.claimDate)).append(",");
        sb.append("\"status\":").append(JsonUtil.toJsonString(this.status));
        sb.append("}");
        return sb.toString();
    }
}
