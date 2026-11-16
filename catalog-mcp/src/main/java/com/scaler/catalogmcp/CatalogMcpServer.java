package com.scaler.catalogmcp;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
public class CatalogMcpServer {
    //2 funcs
    //get to make http call
    //
    private static final String BASE = System.getenv().getOrDefault("CATALOG_URL", "http://localhost:8080");
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final JsonMapper JSON = new JsonMapper();

    interface Action {
        String run() throws Exception;
    }

    private static String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path)).GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) return "Not found.";
        if (response.statusCode() != 200) throw new IllegalStateException("catalog returned HTTP " + response.statusCode());
        return response.body();
    }

    //model will make call to tool (getProduct()): call will necessarally not succeed always
    //tells model. whether call was successfull or not
    // One place that turns any failure into a message the model can read
    private static McpSchema.CallToolResult reply(Action action) {
        try {
            return McpSchema.CallToolResult.builder().addTextContent(action.run()).build();
        } catch (Exception e) {
            return McpSchema.CallToolResult.builder().addTextContent("Tool failed: " + e).isError(true).build();
        }
    }

    public static void main(String[] args) {
        // stdout belongs to the protocol, so we log to System.err only
        System.err.println("catalog-mcp talking to " + BASE);

        //expose 3 functionalities/tools to MCP
        McpSchema.Tool listCategories = McpSchema.Tool.builder()
                .name("list_categories")
                .description("List every product category in the ShopNest catalog, with the number of products in each.")
                .inputSchema(Map.of("type", "object", "properties", Map.of()))
                .build();

        McpSchema.Tool searchProducts = McpSchema.Tool.builder()
                .name("search_products")
                .description("Search ShopNest products by price range, cheapest first unless sort is 'price,desc'. "
                        + "Returns 20 products per page with id, name, price (in dollars) and category. "
                        + "It cannot filter by category or name.")
                .inputSchema(Map.of("type", "object", "properties", Map.of(
                        "minPrice", Map.of("type", "number", "description", "Lowest price in dollars, e.g. 10"),
                        "maxPrice", Map.of("type", "number", "description", "Highest price in dollars, e.g. 50"),
                        "sort", Map.of("type", "string", "description", "Either 'price,asc' or 'price,desc'"),
                        "page", Map.of("type", "integer", "description", "Page number, counting from 0"))))
                .build();

        McpSchema.Tool getProduct = McpSchema.Tool.builder()
                .name("get_product")
                .description("Get one ShopNest product by its numeric id: name, description, price (in dollars) and category.   ")
                .inputSchema(Map.of("type", "object",
                        "properties", Map.of("id", Map.of("type", "integer", "description", "Product id, 1 or higher")),
                        "required", List.of("id")))
                .build();

        McpSchema.Tool averagePrice = McpSchema.Tool.builder()
                .name("average_price_for_category")
                .description("Get the average price in dollars of the active ShopNest products in one category, by category name, e.g. groceries. Returns one number.")
                .inputSchema(Map.of("type", "object",
                        "properties", Map.of("category", Map.of("type", "string", "description", "Category name, e.g. groceries")),
                        "required", List.of("category")))
                .build();

        //block which maps tools to the API calls/logic which needs to be called
        McpServer.sync(new StdioServerTransportProvider(McpJsonDefaults.getMapper()))
                .serverInfo("catalog-mcp", "1.0.0")
                .immediateExecution(true) // answer one call at a time: parallel calls lost replies in our test (free model: cant fire so many requests in parallel to MCP, unable to handle parallel responses)
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                // for each card: the code that runs when the model uses it
                .toolCall(listCategories, (exchange, request) -> reply(() -> get("/categories"))) //which tool makes which API calls
                .toolCall(searchProducts, (exchange, request) -> reply(() -> searchCatalog(request.arguments())))
                .toolCall(getProduct, (exchange, request) -> reply(() -> findProduct(request.arguments())))
                .toolCall(averagePrice, (exchange, request) -> reply(() -> averagePriceFor(request.arguments())))
                .build();
    }
    private static String searchCatalog(Map<String, Object> input) throws Exception {
        // 1. Read what the model sent
        Map<String, Object> a = input == null ? Map.of() : input;
        System.err.println(input);
        // 2. Forward only what the model actually sent; the catalog service applies all defaults
        StringBuilder query = new StringBuilder("/products/search?");
        for (String key : List.of("minPrice", "maxPrice", "sort", "page")) {
            Object value = a.get(key);
            if (value != null)
                query.append(key).append("=")
                        .append(java.net.URLEncoder.encode(value.toString(), java.nio.charset.StandardCharsets.UTF_8))
                        .append("&");
        }

        // 3. Call the service, and turn the big JSON answer into short lines for the model
        JsonNode result = JSON.readTree(get(query.toString()));
        long total = result.path("page").path("totalElements").asLong();
        long pageNumber = result.path("page").path("number").asLong();
        StringBuilder out = new StringBuilder(total + " products match. Page " + pageNumber + " (counting from 0):\n");        for (JsonNode p : result.path("content")) {
            long id = p.path("id").asLong();
            String name = p.path("name").asString();
            String price = p.path("price").asString();
            String category = p.path("categoryDto").path("name").asString();
            out.append("#" + id + " " + name + " | $" + price + " | " + category + "\n");
        }
        return out.toString();
    }

    // The code behind the "get_product" card
    private static String findProduct(Map<String, Object> input) throws Exception {
        long id = Long.parseLong(input.get("id").toString());
        return get("/products/" + id);
    }


    private static String averagePriceFor(Map<String, Object> input) throws Exception {
        String category = input.get("category").toString();
        String encoded = java.net.URLEncoder.encode(category, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
        String body = get("/categories/" + encoded);
        return "Not found.".equals(body) ? "No category named " + category : "Average price in " + category + ": $" + body;
    }

}
