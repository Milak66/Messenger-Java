package servers.serversPractice.controllers;

import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import servers.serversPractice.daos.AvatarService;
import servers.serversPractice.dtos.AtUserDto;
import servers.serversPractice.dtos.UserDto;
import servers.serversPractice.dtos.UserLogIn;
import servers.serversPractice.entities.Chat;
import servers.serversPractice.entities.User;
import servers.serversPractice.repositories.ChatRepository;
import servers.serversPractice.repositories.MessageRepository;
import servers.serversPractice.repositories.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserRepository userRepository;
    private final AvatarService avatarService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public UsersController(
            UserRepository userRepository,
            AvatarService avatarService,
            ChatRepository chatRepository,
            MessageRepository messageRepository
    ) {
        this.userRepository = userRepository;
        this.avatarService = avatarService;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Integer userId
    ) {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDto(user));
    }

    @PostMapping("/getUserByProperties")
    public ResponseEntity<UserDto> getUserByProperties(
            @RequestBody @NonNull UserLogIn userLogIn
    ) {

        Optional<User> optionalUser =
                userRepository.findByUsernameAndPassword(
                        userLogIn.username(),
                        userLogIn.password()
                );

        return optionalUser.map(user -> ResponseEntity.ok(toDto(user))).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping("/getUsersByUsername/{userId}")
    public ResponseEntity<List<AtUserDto>> getUsersByUsername(
            @RequestParam String username,
            @PathVariable Integer userId
    ) {

        User user = userRepository.findById(userId).orElse(null);

        List<AtUserDto> users =
                userRepository
                        .findUsersByUsername(username)
                        .stream()
                        .filter(u -> !u.getUsername().equals(user.getUsername()))
                        .map(u -> new AtUserDto(
                                u.getId(),
                                u.getUsername()
                        ))
                        .toList();

        return ResponseEntity.ok(users);
    }

    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(
            @RequestBody User newUser
    ) {

        for (User user : userRepository.findAll()) {

            if (user.getUsername().equals(newUser.getUsername())) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message",
                                "This username is occupied!"
                        ));
            }
        }

        newUser.setLanguage("en");

        if (newUser.getChats() == null) {
            newUser.setChats(new HashSet<>());
        }

        User savedUser = userRepository.save(newUser);

        return ResponseEntity.ok(toDto(savedUser));
    }

    @PatchMapping("/setLanguage/{userId}")
    public ResponseEntity<?> setLanguage(
            @PathVariable Integer userId,
            @RequestParam("language") String language

    ) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setLanguage(language);

        userRepository.save(user);

        return ResponseEntity.ok(toDto(user));
    }

    @PatchMapping("/{userId}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Integer userId,
            @RequestParam("avatar") MultipartFile avatar
    ) {

        try {

            User user = userRepository
                    .findById(userId)
                    .orElse(null);

            if (user == null) {
                return ResponseEntity
                        .notFound()
                        .build();
            }

            if (avatar.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Avatar is empty"
                                )
                        );
            }

            String contentType =
                    avatar.getContentType();

            if (
                    contentType == null ||
                            !contentType.startsWith("image/")
            ) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "File must be an image"
                                )
                        );
            }

            String avatarPath =
                    avatarService.saveAvatar(
                            userId,
                            avatar
                    );

            user.setAvatar(avatarPath);

            userRepository.save(user);

            return ResponseEntity.ok(
                    toDto(user)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Could not save avatar"
                            )
                    );
        }
    }

    @GetMapping("/getUsersByChatId/{chatId}")
    public ResponseEntity<List<AtUserDto>> getUsersByChatId(
            @PathVariable Integer chatId
    ) {

        List<AtUserDto> users =
                userRepository
                        .findUsersByChatId(chatId)
                        .stream()
                        .map(user -> new AtUserDto(
                                user.getId(),
                                user.getUsername()
                        ))
                        .toList();

        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/deleteUser/{userId}")
    @Transactional
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer userId
    ) throws IOException {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Chat> chatsToDelete = chatRepository.findAll()
                .stream()
                .filter(chat ->
                        chat.getMembers() != null &&
                                chat.getMembers().contains(userId)
                )
                .toList();

        for (Chat chat : chatsToDelete) {

            messageRepository.deleteByChat_Id(chat.getId());

            for (Integer memberId : chat.getMembers()) {

                if (!memberId.equals(userId)) {

                    User member = userRepository
                            .findById(memberId)
                            .orElse(null);

                    if (member != null && member.getChats() != null) {
                        member.getChats().remove(chat.getId());
                        userRepository.save(member);
                    }
                }
            }
        }

        chatRepository.deleteAll(chatsToDelete);

        messageRepository.deleteBySender_Id(userId);

        if (user.getAvatar() != null && !user.getAvatar().isBlank()) {

            Path avatarPath = Paths.get("." + user.getAvatar());

            Files.deleteIfExists(avatarPath);
        }

        userRepository.delete(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/allUsers")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/clearUsers")
    public ResponseEntity<Void> clear() {

        userRepository.deleteAll();
        chatRepository.deleteAll();
        messageRepository.deleteAll();

        return ResponseEntity.ok().build();
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .language(user.getLanguage())
                .chats(user.getChats())
                .build();
    }
}