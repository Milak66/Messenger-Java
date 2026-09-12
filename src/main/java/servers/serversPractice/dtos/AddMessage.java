package servers.serversPractice.dtos;

public record AddMessage(
        Integer sender,
        Integer currentChatId,
        String messageText
) {}