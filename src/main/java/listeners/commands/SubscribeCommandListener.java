package listeners.commands;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;

public class SubscribeCommandListener implements SlashCommandHandler {

    private static final String DEFAULT_REPO = "pbarbieri-sfcc/slack-copilot-demo";

    @Override
    public Response apply(SlashCommandRequest req, SlashCommandContext ctx) {
        ctx.logger.info("Subscribe command received");

        String text = req.getPayload().getText();
        String repository = (text == null || text.trim().isEmpty()) ? DEFAULT_REPO : text.trim();

        // Basic validation for repository format (owner/repo)
        if (!repository.matches("^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$")) {
            return ctx.ack(
                    "Invalid repository format. Please use: owner/repo (e.g., pbarbieri-sfcc/slack-copilot-demo)");
        }

        ctx.logger.info("Subscribing to repository: {}", repository);

        String message = String.format(
                ":white_check_mark: Successfully subscribed to *%s*!\n\n"
                        + "You will receive notifications for:\n"
                        + "• New pull requests\n"
                        + "• New issues\n"
                        + "• Repository releases\n\n"
                        + "_Note: This is a demo subscription. In a production environment, this would connect to GitHub webhooks._",
                repository);

        return ctx.ack(message);
    }
}
