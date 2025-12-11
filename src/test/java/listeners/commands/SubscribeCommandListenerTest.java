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
    public void testApplyWithDefaultRepo() {
        // Given
        var payloadMock = mock(SlashCommandPayload.class);
        when(payloadMock.getText()).thenReturn("");

        var reqMock = mock(SlashCommandRequest.class);
        when(reqMock.getPayload()).thenReturn(payloadMock);

        var ctxMock = spy(SlashCommandContext.class);

        // When
        var subscribeCommandCallback = new SubscribeCommandListener();
        var res = subscribeCommandCallback.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("pbarbieri-sfcc/slack-copilot-demo"));
        assertTrue(res.getBody().contains("Successfully subscribed"));
    }

    @Test
    public void testApplyWithCustomRepo() {
        // Given
        var payloadMock = mock(SlashCommandPayload.class);
        when(payloadMock.getText()).thenReturn("owner/custom-repo");

        var reqMock = mock(SlashCommandRequest.class);
        when(reqMock.getPayload()).thenReturn(payloadMock);

        var ctxMock = spy(SlashCommandContext.class);

        // When
        var subscribeCommandCallback = new SubscribeCommandListener();
        var res = subscribeCommandCallback.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("owner/custom-repo"));
        assertTrue(res.getBody().contains("Successfully subscribed"));
    }

    @Test
    public void testApplyWithInvalidRepo() {
        // Given
        var payloadMock = mock(SlashCommandPayload.class);
        when(payloadMock.getText()).thenReturn("invalid-repo-format");

        var reqMock = mock(SlashCommandRequest.class);
        when(reqMock.getPayload()).thenReturn(payloadMock);

        var ctxMock = spy(SlashCommandContext.class);

        // When
        var subscribeCommandCallback = new SubscribeCommandListener();
        var res = subscribeCommandCallback.apply(reqMock, ctxMock);

        // Then
        assertTrue(res.getBody().contains("Invalid repository format"));
    }
}
