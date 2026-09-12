package servers.serversPractice.dtos;

import lombok.Builder;

@Builder
public record ChatDto(
        Integer id,
        String title,
        String avatar
) {}