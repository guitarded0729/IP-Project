package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Claim;
import model.Item;
import model.ItemStatus;
import service.ClaimService;
import service.ItemService;
import util.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;

// HTTP Handler providing live aggregated dashboard metrics
public class StatsHandler implements HttpHandler {

    private final ItemService itemService;
    private final ClaimService claimService;

    public StatsHandler(ItemService itemService, ClaimService claimService) {
        this.itemService = itemService;
        this.claimService = claimService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtil.handlePreflight(exchange)) {
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendError(exchange, 405, "Only GET method is supported for stats.");
            return;
        }

        try {
            ArrayList<Item> allItems = itemService.getAll();
            ArrayList<Claim> allClaims = claimService.getAllClaims();

            int totalLost = 0;
            int totalFound = 0;
            int returnedItems = 0;

            for (int i = 0; i < allItems.size(); i++) {
                Item item = allItems.get(i);
                if ("LOST".equalsIgnoreCase(item.getType())) {
                    totalLost++;
                } else if ("FOUND".equalsIgnoreCase(item.getType())) {
                    totalFound++;
                }
                if (item.getStatus() == ItemStatus.RETURNED) {
                    returnedItems++;
                }
            }

            int pendingClaims = 0;
            for (int i = 0; i < allClaims.size(); i++) {
                Claim claim = allClaims.get(i);
                if ("PENDING".equalsIgnoreCase(claim.getStatus())) {
                    pendingClaims++;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"totalLost\":").append(totalLost).append(",");
            sb.append("\"totalFound\":").append(totalFound).append(",");
            sb.append("\"pendingClaims\":").append(pendingClaims).append(",");
            sb.append("\"returnedItems\":").append(returnedItems);
            sb.append("}");

            HttpUtil.sendJsonResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Error calculating stats: " + e.getMessage());
        }
    }
}
