package service;

import exception.InvalidInputException;
import exception.ItemNotFoundException;
import model.Category;
import model.Item;
import model.ItemStatus;
import util.FileStorage;
import util.JsonUtil;

import java.util.ArrayList;

// OOP CONCEPT: Abstraction (ItemService implements ItemOperations interface)
public class ItemService implements ItemOperations {

    // OOP CONCEPT: Collections (ArrayList storing Item objects)
    private final ArrayList<Item> items;
    private final FileStorage fileStorage;

    // OOP CONCEPT: Parameterized Constructor
    public ItemService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.items = fileStorage.loadItems();
    }

    // OOP CONCEPT: Polymorphism (Implementing interface method)
    @Override
    public synchronized void add(Item item) throws InvalidInputException {
        if (item == null) {
            throw new InvalidInputException("Item cannot be null.");
        }
        items.add(item);
        fileStorage.saveItems(items);
    }

    // OOP CONCEPT: Polymorphism (Implementing interface method)
    @Override
    public synchronized ArrayList<Item> getAll() {
        return new ArrayList<Item>(items);
    }

    // OOP CONCEPT: Polymorphism (Implementing interface method)
    @Override
    public synchronized Item getById(int id) throws ItemNotFoundException {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getId() == id) {
                return item;
            }
        }
        throw new ItemNotFoundException("Item with ID " + id + " was not found.");
    }

    // OOP CONCEPT: Polymorphism (Implementing interface method)
    @Override
    public synchronized void update(Item item) throws ItemNotFoundException, InvalidInputException {
        if (item == null) {
            throw new InvalidInputException("Item cannot be null.");
        }
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == item.getId()) {
                items.set(i, item);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new ItemNotFoundException("Cannot update. Item with ID " + item.getId() + " not found.");
        }
        fileStorage.saveItems(items);
    }

    // OOP CONCEPT: Polymorphism (Implementing interface method)
    @Override
    public synchronized void delete(int id) throws ItemNotFoundException {
        boolean removed = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id) {
                items.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            throw new ItemNotFoundException("Cannot delete. Item with ID " + id + " not found.");
        }
        fileStorage.saveItems(items);
    }

    // OOP CONCEPT: Polymorphism (Method Overloading - 1 argument)
    @Override
    public synchronized ArrayList<Item> search(String keyword) {
        ArrayList<Item> results = new ArrayList<Item>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            boolean titleMatches = item.getTitle().toLowerCase().contains(lowerKeyword);
            boolean descMatches = item.getDescription().toLowerCase().contains(lowerKeyword);
            boolean locMatches = item.getLocation().toLowerCase().contains(lowerKeyword);
            if (titleMatches || descMatches || locMatches) {
                results.add(item);
            }
        }
        return results;
    }

    // OOP CONCEPT: Polymorphism (Method Overloading - 2 arguments)
    @Override
    public synchronized ArrayList<Item> search(String keyword, Category category) {
        ArrayList<Item> results = new ArrayList<Item>();
        ArrayList<Item> keywordMatches = search(keyword); // Reusing search(keyword)
        for (int i = 0; i < keywordMatches.size(); i++) {
            Item item = keywordMatches.get(i);
            if (category == null || item.getCategory() == category) {
                results.add(item);
            }
        }
        return results;
    }

    // Flexible multi-parameter filter for the frontend Browse Items view
    public synchronized ArrayList<Item> filter(String type, Category category, ItemStatus status, String keyword) {
        ArrayList<Item> results = new ArrayList<Item>();
        String lowerKeyword = (keyword != null) ? keyword.trim().toLowerCase() : "";

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);

            // Filter by type (LOST / FOUND)
            if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
                if (!item.getType().equalsIgnoreCase(type.trim())) {
                    continue;
                }
            }

            // Filter by category
            if (category != null) {
                if (item.getCategory() != category) {
                    continue;
                }
            }

            // Filter by status (OPEN / CLAIM_PENDING / RETURNED)
            if (status != null) {
                if (item.getStatus() != status) {
                    continue;
                }
            }

            // Filter by keyword in title, description, or location
            if (!lowerKeyword.isEmpty()) {
                boolean titleMatches = item.getTitle().toLowerCase().contains(lowerKeyword);
                boolean descMatches = item.getDescription().toLowerCase().contains(lowerKeyword);
                boolean locMatches = item.getLocation().toLowerCase().contains(lowerKeyword);
                if (!titleMatches && !descMatches && !locMatches) {
                    continue;
                }
            }

            results.add(item);
        }
        return results;
    }

    // Smart Match Algorithm: Suggests potential matches of opposite type with match scoring
    public synchronized ArrayList<String> findMatchesJson(int itemId) throws ItemNotFoundException {
        Item source = getById(itemId);
        String oppositeType = source.getType().equalsIgnoreCase("LOST") ? "FOUND" : "LOST";

        ArrayList<Item> candidates = new ArrayList<Item>();
        ArrayList<Integer> scores = new ArrayList<Integer>();
        ArrayList<String> reasonsList = new ArrayList<String>();

        // Common stop words to ignore when comparing descriptions/titles
        String[] stopWords = {"the", "and", "with", "for", "from", "near", "this", "that", "have", "has", "lost", "found", "item"};

        for (int i = 0; i < items.size(); i++) {
            Item candidate = items.get(i);
            // Must be opposite type and not returned
            if (!candidate.getType().equalsIgnoreCase(oppositeType) || candidate.getStatus() == ItemStatus.RETURNED) {
                continue;
            }

            int score = 0;
            StringBuilder reasons = new StringBuilder();

            // 1. Same category match (+40 points)
            if (candidate.getCategory() == source.getCategory()) {
                score += 40;
                reasons.append("Identical Category (").append(source.getCategory().getDisplayName()).append("); ");
            }

            // 2. Same campus location match (+30 points)
            if (candidate.getLocation().equalsIgnoreCase(source.getLocation())) {
                score += 30;
                reasons.append("Same Campus Location (").append(source.getLocation()).append("); ");
            }

            // 3. Keyword similarity in title and description (+10 points per shared keyword, max 30)
            String sourceText = (source.getTitle() + " " + source.getDescription()).toLowerCase();
            String candidateText = (candidate.getTitle() + " " + candidate.getDescription()).toLowerCase();
            String[] sourceWords = sourceText.split("[^a-zA-Z0-9]+");

            int sharedKeywordScore = 0;
            ArrayList<String> matchedWords = new ArrayList<String>();

            for (int w = 0; w < sourceWords.length; w++) {
                String word = sourceWords[w].trim();
                if (word.length() < 3) {
                    continue;
                }
                boolean isStopWord = false;
                for (int s = 0; s < stopWords.length; s++) {
                    if (word.equals(stopWords[s])) {
                        isStopWord = true;
                        break;
                    }
                }
                if (isStopWord || matchedWords.contains(word)) {
                    continue;
                }

                // Check if candidate contains this word
                if (candidateText.contains(word)) {
                    matchedWords.add(word);
                    sharedKeywordScore += 10;
                    if (sharedKeywordScore >= 30) {
                        break;
                    }
                }
            }

            if (sharedKeywordScore > 0) {
                score += sharedKeywordScore;
                reasons.append("Shared Keywords (");
                for (int m = 0; m < matchedWords.size(); m++) {
                    reasons.append(matchedWords.get(m));
                    if (m < matchedWords.size() - 1) {
                        reasons.append(", ");
                    }
                }
                reasons.append("); ");
            }

            // Only consider reasonable matches (score >= 30)
            if (score >= 30) {
                candidates.add(candidate);
                scores.add(Math.min(score, 100)); // Cap at 100%
                reasonsList.add(reasons.toString());
            }
        }

        // Sort descending by score using simple bubble sort
        for (int i = 0; i < scores.size() - 1; i++) {
            for (int j = 0; j < scores.size() - i - 1; j++) {
                if (scores.get(j) < scores.get(j + 1)) {
                    // Swap scores
                    int tempScore = scores.get(j);
                    scores.set(j, scores.get(j + 1));
                    scores.set(j + 1, tempScore);

                    // Swap candidates
                    Item tempItem = candidates.get(j);
                    candidates.set(j, candidates.get(j + 1));
                    candidates.set(j + 1, tempItem);

                    // Swap reasons
                    String tempReason = reasonsList.get(j);
                    reasonsList.set(j, reasonsList.get(j + 1));
                    reasonsList.set(j + 1, tempReason);
                }
            }
        }

        // Build JSON representation
        ArrayList<String> matchesJson = new ArrayList<String>();
        for (int i = 0; i < candidates.size(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"score\":").append(scores.get(i)).append(",");
            sb.append("\"reasons\":").append(JsonUtil.toJsonString(reasonsList.get(i))).append(",");
            sb.append("\"item\":").append(candidates.get(i).toJson());
            sb.append("}");
            matchesJson.add(sb.toString());
        }

        return matchesJson;
    }
}
