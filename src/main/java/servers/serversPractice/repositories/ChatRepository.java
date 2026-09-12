package servers.serversPractice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import servers.serversPractice.entities.Chat;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
}