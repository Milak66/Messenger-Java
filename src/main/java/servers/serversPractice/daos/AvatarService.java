package servers.serversPractice.daos;

import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Repository
public class AvatarService {

    private final Path avatarDirectory =
            Paths.get("uploads/avatars");

    public String saveAvatar(
            Integer userId,
            MultipartFile file
    ) throws IOException {

        Files.createDirectories(avatarDirectory);

        String filename =
                userId + ".webp";

        Path destination =
                avatarDirectory.resolve(filename);

        Files.write(
                destination,
                file.getBytes()
        );

        return "https://telegrammessage-java.onrender.com/uploads/avatars/" + filename;
    }
}