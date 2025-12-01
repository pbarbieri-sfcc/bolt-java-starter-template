package utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserDataServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private UserDataService userDataService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userDataService = new UserDataService(mockHttpClient);
    }

    @Test
    public void testGetUserData_WithValidId_ReturnsUserData() throws ExecutionException, InterruptedException {
        // Given
        String userId = "123";
        String expectedData = "{\"id\":\"123\",\"name\":\"John Doe\",\"email\":\"john@example.com\"}";

        when(mockResponse.body()).thenReturn(expectedData);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<String> result = userDataService.getUserData(userId);
        String actualData = result.get();

        // Then
        assertEquals(expectedData, actualData);
        verify(mockHttpClient, times(1)).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void testGetUserData_WithNullId_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userDataService.getUserData(null);
        });
    }

    @Test
    public void testGetUserData_WithEmptyId_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userDataService.getUserData("");
        });
    }

    @Test
    public void testGetUserData_WithWhitespaceId_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userDataService.getUserData("   ");
        });
    }

    @Test
    public void testGetUserData_WithApiError_HandlesException() {
        // Given
        String userId = "456";
        CompletableFuture<HttpResponse<String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IOException("Network error"));

        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(failedFuture);

        // When
        CompletableFuture<String> result = userDataService.getUserData(userId);

        // Then
        assertThrows(ExecutionException.class, result::get);
    }

    @Test
    public void testGetUserDataSync_WithValidId_ReturnsUserData() throws IOException, InterruptedException {
        // Given
        String userId = "789";
        String expectedData = "{\"id\":\"789\",\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}";

        when(mockResponse.body()).thenReturn(expectedData);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // When
        String actualData = userDataService.getUserDataSync(userId);

        // Then
        assertEquals(expectedData, actualData);
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void testGetUserDataSync_WithNullId_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userDataService.getUserDataSync(null);
        });
    }

    @Test
    public void testGetUserDataSync_WithEmptyId_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userDataService.getUserDataSync("");
        });
    }

    @Test
    public void testGetUserDataSync_WithApiError_ThrowsException() throws IOException, InterruptedException {
        // Given
        String userId = "999";
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection timeout"));

        // When & Then
        assertThrows(IOException.class, () -> {
            userDataService.getUserDataSync(userId);
        });
    }

    @Test
    public void testDefaultConstructor_CreatesInstance() {
        // When
        UserDataService service = new UserDataService();

        // Then
        assertNotNull(service);
    }
}
