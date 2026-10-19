package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.InvalidInputException;
import exception.ItemNotFoundException;
import exception.UnauthorizedException;
import model.Claim;
import service.AuthService;
import service.ClaimService;
import service.ItemService;
import util.HttpUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

// HTTP Handler managing claim creation, listing, approval, and rejection
public class ClaimHandler implements HttpHandler {

    private final ClaimService claimService;
    private final ItemService itemService;
    private final AuthService authService;

    public ClaimHandler(ClaimService claimService, ItemService itemService, AuthService authService) {
        this.claimService = claimService;
        this.itemService = itemService;
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtil.handlePreflight(exchange)) {
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handlePut(exchange, path);
            } else {
                HttpUtil.sendError(exchange, 405, "Method " + method + " not supported.");
            }
        } catch (InvalidInputException iie) {
            HttpUtil.sendError(exchange, 400, iie.getMessage());
        } catch (UnauthorizedException ue) {
            HttpUtil.sendError(exchange, 401, ue.getMessage());
        } catch (ItemNotFoundException infe) {
            HttpUtil.sendError(exchange, 404, infe.getMessage());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException, UnauthorizedException {
        // Enforce Admin token check to view all claims
        String token = HttpUtil.getAdminToken(exchange);
        if (!authService.isValidToken(token)) {
            throw new UnauthorizedException("Admin authorization token is missing or invalid.");
        }

        ArrayList<Claim> claims = claimService.getAllClaims();
        ArrayList<String> jsonClaims = new ArrayList<String>();
        for (int i = 0; i < claims.size(); i++) {
            jsonClaims.add(claims.get(i).toJson());
        }

        HttpUtil.sendJsonResponse(exchange, 200, JsonUtil.toJsonArray(jsonClaims));
    }

    private void handlePost(HttpExchange exchange) throws IOException, InvalidInputException, ItemNotFoundException {
        Map<String, String> params = HttpUtil.parseFormData(exchange);

        String itemIdStr = params.get("itemId");
        String claimantName = params.get("claimantName");
        String claimantIdNumber = params.get("claimantIdNumber");
        String claimantContact = params.get("claimantContact");
        String proofDetails = params.get("proofDetails");
        String claimDate = params.get("claimDate");

        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            throw new InvalidInputException("Item ID is required to file a claim.");
        }

        int itemId;
        try {
            itemId = Integer.parseInt(itemIdStr.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Item ID must be a numeric integer.");
        }

        int claimId = Claim.generateNextId();
        Claim claim = new Claim(claimId, itemId, claimantName, claimantIdNumber, claimantContact, proofDetails, claimDate, "PENDING");

        claimService.addClaim(claim, itemService);
        HttpUtil.sendJsonResponse(exchange, 201, claim.toJson());
    }

    private void handlePut(HttpExchange exchange, String path) 
            throws IOException, UnauthorizedException, ItemNotFoundException, InvalidInputException {
        // Enforce Admin token check
        String token = HttpUtil.getAdminToken(exchange);
        if (!authService.isValidToken(token)) {
            throw new UnauthorizedException("Admin authorization token is missing or invalid.");
        }

        String[] segments = path.split("/");
        if (segments.length < 4) {
            throw new InvalidInputException("Claim ID is required in URL path.");
        }
        int claimId = Integer.parseInt(segments[3]);

        Map<String, String> params = HttpUtil.parseFormData(exchange);
        String action = params.get("action");

        claimService.processClaim(claimId, action, itemService);
        String resultMsg = "APPROVE".equalsIgnoreCase(action) ? "APPROVED" : "REJECTED";
        HttpUtil.sendJsonResponse(exchange, 200, "{\"message\":\"Claim #" + claimId + " marked as " + resultMsg + "\"}");
    }
}
