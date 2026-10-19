package util;

import exception.InvalidInputException;
import model.*;

import java.io.*;
import java.util.ArrayList;

// Utility class for file persistence using standard Java File I/O (BufferedReader / BufferedWriter)
public class FileStorage {

    private final String itemsFilePath;
    private final String claimsFilePath;

    public FileStorage(String itemsFilePath, String claimsFilePath) {
        this.itemsFilePath = itemsFilePath;
        this.claimsFilePath = claimsFilePath;
        initStorage();
    }

    // Sanitizes fields so delimiter character '|' or newlines don't break file format
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("|", "-").replace("\r", " ").replace("\n", " ").trim();
    }

    // Checks and initializes data directory and files with sample data if empty
    private void initStorage() {
        File itemsFile = new File(itemsFilePath);
        File claimsFile = new File(claimsFilePath);

        // Ensure parent folder exists
        if (itemsFile.getParentFile() != null && !itemsFile.getParentFile().exists()) {
            itemsFile.getParentFile().mkdirs();
        }

        if (!itemsFile.exists() || itemsFile.length() == 0) {
            createSampleItemsFile(itemsFile);
        }

        if (!claimsFile.exists() || claimsFile.length() == 0) {
            createSampleClaimsFile(claimsFile);
        }
    }

    private void createSampleItemsFile(File file) {
        ArrayList<Item> sampleItems = new ArrayList<Item>();
        try {
            // OOP CONCEPT: Composition (Items have Student or Staff reporters)
            User u1 = new Staff("STF102", "Dr. Sharma", "9876543210");
            User u2 = new Student("2024CS082", "Rahul Verma", "9811223344");
            User u3 = new Student("2023EC041", "Priya Nair", "9822334455");
            User u4 = new Student("2024ME019", "Rohan Das", "9833445566");
            User u5 = new Staff("STF204", "Mr. K. Rao", "9844556677");
            User u6 = new Student("2023CS110", "Ananya Sen", "9855667788");
            User u7 = new Staff("STF055", "Security Desk", "9866778899");
            User u8 = new Student("2024BT015", "Sneha Roy", "9877889900");
            User u9 = new Staff("STF102", "Dr. Sharma", "9876543210");
            User u10 = new Student("2024CS033", "Vikram Joshi", "9888990011");

            // OOP CONCEPT: Polymorphism (Parent-type reference Item holding LostItem and FoundItem)
            sampleItems.add(new FoundItem(1, "Casio FX-991EX Scientific Calculator", "Black casing with yellow sticker on battery lid", Category.ELECTRONICS, "Labs", "2026-10-14", ItemStatus.OPEN, u1));
            sampleItems.add(new LostItem(2, "Dell Inspiron 15 Laptop", "Silver laptop with Intel i5 sticker and minor scratch on top left", Category.ELECTRONICS, "Library", "2026-10-15", ItemStatus.OPEN, u2));
            sampleItems.add(new FoundItem(3, "Apple iPhone 13 (Blue)", "Blue silicone cover with transparent ring holder, locked screen", Category.ELECTRONICS, "Canteen", "2026-10-16", ItemStatus.CLAIM_PENDING, u3));
            sampleItems.add(new LostItem(4, "Titan Karishma Analog Watch", "Silver metallic strap with black round dial and date display", Category.ACCESSORIES, "Sports Complex", "2026-10-13", ItemStatus.OPEN, u4));
            sampleItems.add(new FoundItem(5, "College ID Card - Amit Patel", "Computer Science department student ID card with blue lanyard", Category.ID_CARDS, "Admin Block", "2026-10-17", ItemStatus.RETURNED, u5));
            sampleItems.add(new LostItem(6, "Wildcraft Backpack (Dark Blue)", "Contains 3 engineering notebooks, lab record, and glasses case", Category.BAGS, "Library", "2026-10-16", ItemStatus.OPEN, u6));
            sampleItems.add(new FoundItem(7, "Set of Bike Keys with Honda Keychain", "Two silver keys attached to a red rubber Honda wings keychain", Category.KEYS, "Parking", "2026-10-17", ItemStatus.OPEN, u7));
            sampleItems.add(new LostItem(8, "Fastrack Blue Polarized Sunglasses", "Square aviator frames in hard zippered black pouch", Category.ACCESSORIES, "Canteen", "2026-10-15", ItemStatus.OPEN, u8));
            sampleItems.add(new FoundItem(9, "Data Structures and Algorithms Book", "Textbook by Robert Lafore with pencil notes on page 45", Category.BOOKS, "Library", "2026-10-16", ItemStatus.OPEN, u9));
            sampleItems.add(new LostItem(10, "Navy Blue Puma Hoodie", "Size L zipper hoodie with white Puma logo on chest", Category.CLOTHING, "Sports Complex", "2026-10-14", ItemStatus.OPEN, u10));

            saveItems(sampleItems);
        } catch (Exception e) {
            System.err.println("Error initializing sample items: " + e.getMessage());
        }
    }

    private void createSampleClaimsFile(File file) {
        ArrayList<Claim> sampleClaims = new ArrayList<Claim>();
        try {
            sampleClaims.add(new Claim(1, 3, "Aditya Mehta", "2023EC099", "9899001122", "Lock screen wallpaper has photo of my golden retriever; phone color is blue.", "2026-10-17", "PENDING"));
            sampleClaims.add(new Claim(2, 5, "Amit Patel", "2024CS005", "9876501234", "It is my official college ID card showing roll number 2024CS005.", "2026-10-17", "APPROVED"));
            sampleClaims.add(new Claim(3, 1, "Kunal Bansal", "2024EE044", "9812345678", "Left during practical lab exam; initials KB engraved on the plastic back.", "2026-10-15", "REJECTED"));

            saveClaims(sampleClaims);
        } catch (Exception e) {
            System.err.println("Error initializing sample claims: " + e.getMessage());
        }
    }

    // OOP CONCEPT: File I/O with BufferedReader
    public ArrayList<Item> loadItems() {
        ArrayList<Item> items = new ArrayList<Item>();
        File file = new File(itemsFilePath);
        if (!file.exists()) {
            return items;
        }

        BufferedReader reader = null;
        int maxId = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 12) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        String type = parts[1];
                        String title = parts[2];
                        String description = parts[3];
                        Category category = Category.fromString(parts[4]);
                        String location = parts[5];
                        String date = parts[6];
                        ItemStatus status = ItemStatus.fromString(parts[7]);
                        String reporterRole = parts[8];
                        String reporterId = parts[9];
                        String reporterName = parts[10];
                        String reporterContact = parts[11];

                        // OOP CONCEPT: Polymorphism (Creating Student or Staff based on role)
                        User reporter;
                        if ("STAFF".equalsIgnoreCase(reporterRole)) {
                            reporter = new Staff(reporterId, reporterName, reporterContact);
                        } else {
                            reporter = new Student(reporterId, reporterName, reporterContact);
                        }

                        // OOP CONCEPT: Polymorphism (Creating LostItem or FoundItem)
                        Item item;
                        if ("LOST".equalsIgnoreCase(type)) {
                            item = new LostItem(id, title, description, category, location, date, status, reporter);
                        } else {
                            item = new FoundItem(id, title, description, category, location, date, status, reporter);
                        }

                        items.add(item);
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception ex) {
                        System.err.println("Skipping malformed item record: " + ex.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading items file: " + e.getMessage());
        } finally {
            // OOP CONCEPT: Exception handling (finally block for resource cleanup)
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }

        Item.updateIdCounter(maxId);
        return items;
    }

    // OOP CONCEPT: File I/O with BufferedWriter
    public synchronized void saveItems(ArrayList<Item> items) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(itemsFilePath));
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                User reporter = item.getReporter();

                StringBuilder sb = new StringBuilder();
                sb.append(item.getId()).append("|");
                sb.append(sanitize(item.getType())).append("|");
                sb.append(sanitize(item.getTitle())).append("|");
                sb.append(sanitize(item.getDescription())).append("|");
                sb.append(sanitize(item.getCategory().name())).append("|");
                sb.append(sanitize(item.getLocation())).append("|");
                sb.append(sanitize(item.getDate())).append("|");
                sb.append(sanitize(item.getStatus().name())).append("|");
                sb.append(reporter != null ? sanitize(reporter.getRole()) : "STUDENT").append("|");
                sb.append(reporter != null ? sanitize(reporter.getIdNumber()) : "").append("|");
                sb.append(reporter != null ? sanitize(reporter.getName()) : "").append("|");
                sb.append(reporter != null ? sanitize(reporter.getContact()) : "");

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing items file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // OOP CONCEPT: File I/O with BufferedReader
    public ArrayList<Claim> loadClaims() {
        ArrayList<Claim> claims = new ArrayList<Claim>();
        File file = new File(claimsFilePath);
        if (!file.exists()) {
            return claims;
        }

        BufferedReader reader = null;
        int maxId = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        int itemId = Integer.parseInt(parts[1]);
                        String claimantName = parts[2];
                        String claimantIdNumber = parts[3];
                        String claimantContact = parts[4];
                        String proofDetails = parts[5];
                        String claimDate = parts[6];
                        String status = parts[7];

                        Claim claim = new Claim(id, itemId, claimantName, claimantIdNumber, claimantContact, proofDetails, claimDate, status);
                        claims.add(claim);
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (Exception ex) {
                        System.err.println("Skipping malformed claim record: " + ex.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading claims file: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }

        Claim.updateClaimIdCounter(maxId);
        return claims;
    }

    // OOP CONCEPT: File I/O with BufferedWriter
    public synchronized void saveClaims(ArrayList<Claim> claims) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(claimsFilePath));
            for (int i = 0; i < claims.size(); i++) {
                Claim claim = claims.get(i);
                StringBuilder sb = new StringBuilder();
                sb.append(claim.getId()).append("|");
                sb.append(claim.getItemId()).append("|");
                sb.append(sanitize(claim.getClaimantName())).append("|");
                sb.append(sanitize(claim.getClaimantIdNumber())).append("|");
                sb.append(sanitize(claim.getClaimantContact())).append("|");
                sb.append(sanitize(claim.getProofDetails())).append("|");
                sb.append(sanitize(claim.getClaimDate())).append("|");
                sb.append(sanitize(claim.getStatus()));

                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing claims file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
