package servers.serversPractice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import servers.serversPractice.entities.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findByChat_Id(Integer chatId);

    void deleteByChat_Id(Integer chatId);
}