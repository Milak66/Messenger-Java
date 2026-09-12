package servers.serversPractice.dtos;

import lombok.Builder;
import java.util.Set;

@Builder
public record UserDto(
        Integer id,
        String username,
        String nickname,
        String avatar,
        String language,
        Set<Integer> chats
) {}