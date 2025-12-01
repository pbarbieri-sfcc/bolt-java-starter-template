package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for fetching user data from an external API.
 * This class demonstrates proper async API calls with error handling.
 *
 * BUGS FIXED (Originally similar to the JavaScript example):
 * 1. Added proper async handling with CompletableFuture
 * 2. Added required 'id' parameter validation
 * 3. Added proper error handling
 * 4. Added null checks and input validation
 */
public class UserDataService {
    private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);
    private static final String API_BASE_URL = "https://api.example.com/users/";
    private final HttpClient httpClient;

    public UserDataService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Constructor for testing with custom HttpClient
    public UserDataService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Fetches user data asynchronously from the API.
     *
     * @param id The user ID to fetch data for (must not be null or empty)
     * @return CompletableFuture containing the user data as a JSON string
     * @throws IllegalArgumentException if id is null or empty
     */
    public CompletableFuture<String> getUserData(String id) {
        // Bug fix #1: Validate required parameter
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // Bug fix #2: Proper async handling
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + id))
                .GET()
                .build();

        return httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    logger.error("Error fetching user data for id: {}", id, ex);
                    throw new RuntimeException("Failed to fetch user data: " + ex.getMessage(), ex);
                });
    }

    /**
     * Synchronous version of getUserData for simpler use cases.
     *
     * @param id The user ID to fetch data for
     * @return The user data as a JSON string
     * @throws IllegalArgumentException if id is null or empty
     * @throws IOException if the request fails
     * @throws InterruptedException if the operation is interrupted
     */
    public String getUserDataSync(String id) throws IOException, InterruptedException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
