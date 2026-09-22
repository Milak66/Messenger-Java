package servers.serversPractice.dtos;

public record TypingDto(
        Integer chatId,
        Integer userId,
        boolean typing
) {
}