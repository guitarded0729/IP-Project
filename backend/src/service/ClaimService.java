package service;

import exception.InvalidInputException;
import exception.ItemNotFoundException;
import model.Claim;
import model.Item;
import model.ItemStatus;
import util.FileStorage;

import java.util.ArrayList;

// Service layer handling claim operations and status transitions
public class ClaimService {

    // OOP CONCEPT: Collections (ArrayList storing Claim objects)
    private final ArrayList<Claim> claims;
    private final FileStorage fileStorage;

    public ClaimService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.claims = fileStorage.loadClaims();
    }

    // Submit a new claim for a found item
    public synchronized void addClaim(Claim claim, ItemService itemService) throws InvalidInputException, ItemNotFoundException {
        if (claim == null) {
            throw new InvalidInputException("Claim cannot be null.");
        }

        // Verify that the referenced item exists
        Item item = itemService.getById(claim.getItemId());

        if (!"FOUND".equalsIgnoreCase(item.getType())) {
            throw new InvalidInputException("Claims can only be filed against FOUND items.");
        }

        if (item.getStatus() == ItemStatus.RETURNED) {
            throw new InvalidInputException("This item has already been marked as RETURNED.");
        }

        claims.add(claim);
        fileStorage.saveClaims(claims);

        // Update item status to CLAIM_PENDING
        item.setStatus(ItemStatus.CLAIM_PENDING);
        itemService.update(item);
    }

    public synchronized ArrayList<Claim> getAllClaims() {
        return new ArrayList<Claim>(claims);
    }

    public synchronized Claim getById(int id) throws ItemNotFoundException {
        for (int i = 0; i < claims.size(); i++) {
            Claim claim = claims.get(i);
            if (claim.getId() == id) {
                return claim;
            }
        }
        throw new ItemNotFoundException("Claim with ID " + id + " was not found.");
    }

    // Process a claim (APPROVE or REJECT)
    public synchronized void processClaim(int claimId, String action, ItemService itemService) 
            throws InvalidInputException, ItemNotFoundException {
        if (action == null || (!action.equalsIgnoreCase("APPROVE") && !action.equalsIgnoreCase("REJECT"))) {
            throw new InvalidInputException("Action must be either APPROVE or REJECT.");
        }

        Claim claim = getById(claimId);
        Item item = itemService.getById(claim.getItemId());

        if (action.equalsIgnoreCase("APPROVE")) {
            claim.setStatus("APPROVED");
            // Approving a claim marks the item RETURNED
            item.setStatus(ItemStatus.RETURNED);
        } else {
            claim.setStatus("REJECTED");
            // Check if there are any other pending claims for this item
            boolean hasOtherPending = false;
            for (int i = 0; i < claims.size(); i++) {
                Claim c = claims.get(i);
                if (c.getItemId() == item.getId() && c.getId() != claimId && "PENDING".equalsIgnoreCase(c.getStatus())) {
                    hasOtherPending = true;
                    break;
                }
            }
            if (!hasOtherPending && item.getStatus() != ItemStatus.RETURNED) {
                item.setStatus(ItemStatus.OPEN);
            }
        }

        fileStorage.saveClaims(claims);
        itemService.update(item);
    }
}
