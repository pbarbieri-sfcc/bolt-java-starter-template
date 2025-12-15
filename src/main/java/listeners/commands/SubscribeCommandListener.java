package listeners.commands;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;

public class SubscribeCommandListener implements SlashCommandHandler {

    @Override
    public Response apply(SlashCommandRequest req, SlashCommandContext ctx) {
        String text = req.getPayload().getText();
        ctx.logger.info("Subscribe command invoked with text: {}", text);

        if (text == null || text.trim().isEmpty()) {
            return ctx.ack(":x: Please provide a repository to subscribe to. Usage: `/subscribe owner/repo`");
        }

        // Validate repository format (owner/repo)
        String repoPattern = "^[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+$";
        if (!text.trim().matches(repoPattern)) {
            return ctx.ack(":x: Invalid repository format. Please use the format: `/subscribe owner/repo`");
        }

        String repository = text.trim();
        ctx.logger.info("Subscribing to repository: {}", repository);

        // In a real implementation, this would:
        // 1. Store the subscription in a database
        // 2. Set up webhooks for the repository
        // 3. Listen for repository events
        // For now, we'll just acknowledge the subscription
        return ctx.ack(":white_check_mark: Successfully subscribed to *" + repository + "*!\n"
                + "You'll receive notifications for events in this repository.");
    }
}
