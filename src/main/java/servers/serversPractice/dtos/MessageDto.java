package servers.serversPractice.dtos;

import lombok.Builder;

@Builder public record MessageDto(
        Integer id,
        String text,
        UserDto sender,
        String sendTime
) {}