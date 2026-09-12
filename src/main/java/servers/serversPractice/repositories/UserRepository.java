package servers.serversPractice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import servers.serversPractice.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsernameAndPassword(
            String username,
            String password
    );

    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT(:username, '%'))
            """)
    List<User> findUsersByUsername(
            @Param("username") String username
    );

    @Query(
            value = """
                SELECT *
                FROM users
                WHERE :chatId = ANY(chats)
                """,
            nativeQuery = true
    )
    List<User> findUsersByChatId(
            @Param("chatId") Integer chatId
    );
}