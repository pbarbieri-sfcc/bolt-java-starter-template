package listeners.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import org.junit.jupiter.api.Test;

public class SubscribeCommandListenerTest {

    @Test
    public void testApplyWithValidRepository() {
        // Given
        var reqMock = mock(SlashCommandRequest.class);
        var payloadMock = mock(SlashCommandPayload.class);
        var ctxMock = spy(SlashCommandContext.class);

        when(reqMock.getPayload()).thenReturn(payloadMock);
        when(payloadMock.getText()).thenReturn("pbarbieri-sfcc/slack-copilot-demo-cd");

        // When
        var subscribeCommandListener = new SubscribeCommandListener();
        var res = subscribeCommandListener.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("Successfully subscribed"));
        assertTrue(res.getBody().contains("pbarbieri-sfcc/slack-copilot-demo-cd"));
    }

    @Test
    public void testApplyWithEmptyText() {
        // Given
        var reqMock = mock(SlashCommandRequest.class);
        var payloadMock = mock(SlashCommandPayload.class);
        var ctxMock = spy(SlashCommandContext.class);

        when(reqMock.getPayload()).thenReturn(payloadMock);
        when(payloadMock.getText()).thenReturn("");

        // When
        var subscribeCommandListener = new SubscribeCommandListener();
        var res = subscribeCommandListener.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("Please provide a repository"));
    }

    @Test
    public void testApplyWithInvalidFormat() {
        // Given
        var reqMock = mock(SlashCommandRequest.class);
        var payloadMock = mock(SlashCommandPayload.class);
        var ctxMock = spy(SlashCommandContext.class);

        when(reqMock.getPayload()).thenReturn(payloadMock);
        when(payloadMock.getText()).thenReturn("invalid-format");

        // When
        var subscribeCommandListener = new SubscribeCommandListener();
        var res = subscribeCommandListener.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("Invalid repository format"));
    }

    @Test
    public void testApplyWithRepositoryContainingDots() {
        // Given
        var reqMock = mock(SlashCommandRequest.class);
        var payloadMock = mock(SlashCommandPayload.class);
        var ctxMock = spy(SlashCommandContext.class);

        when(reqMock.getPayload()).thenReturn(payloadMock);
        when(payloadMock.getText()).thenReturn("owner/my-project.io");

        // When
        var subscribeCommandListener = new SubscribeCommandListener();
        var res = subscribeCommandListener.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("Successfully subscribed"));
        assertTrue(res.getBody().contains("my-project.io"));
    }

    @Test
    public void testApplyWithRepositoryStartingWithInvalidChar() {
        // Given
        var reqMock = mock(SlashCommandRequest.class);
        var payloadMock = mock(SlashCommandPayload.class);
        var ctxMock = spy(SlashCommandContext.class);

        when(reqMock.getPayload()).thenReturn(payloadMock);
        when(payloadMock.getText()).thenReturn("owner/-invalid-repo");

        // When
        var subscribeCommandListener = new SubscribeCommandListener();
        var res = subscribeCommandListener.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("Invalid repository format"));
    }
}
