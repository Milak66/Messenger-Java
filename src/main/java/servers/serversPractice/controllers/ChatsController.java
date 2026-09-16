package servers.serversPractice.controllers;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import servers.serversPractice.dtos.ChatDto;
import servers.serversPractice.entities.Chat;
import servers.serversPractice.entities.Message;
import servers.serversPractice.entities.User;
import servers.serversPractice.repositories.ChatRepository;
import servers.serversPractice.repositories.MessageRepository;
import servers.serversPractice.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chats")
public class ChatsController {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatsController(
            ChatRepository chatRepository,
            UserRepository userRepository,
            MessageRepository messageRepository
    ) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @PostMapping("/createChat/{userId}")
    @Transactional
    public ResponseEntity<Integer> createChat(
            @PathVariable Integer userId,
            @RequestBody Integer otherUserId
    ) {

        User user = userRepository.findById(userId)
                .orElse(null);

        User otherUser = userRepository.findById(otherUserId)
                .orElse(null);

        if (user == null || otherUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (userId.equals(otherUserId)) {
            return ResponseEntity.badRequest().build();
        }

        for (Integer chatId : user.getChats()) {

            Chat chat = chatRepository.findById(chatId)
                    .orElse(null);

            if (chat != null && chat.getMembers().contains(otherUserId)) {
                return ResponseEntity.ok(chat.getId());
            }
        }

        Chat chat = new Chat();

        chat.getMembers().add(userId);
        chat.getMembers().add(otherUserId);

        Chat savedChat = chatRepository.save(chat);

        user.getChats().add(savedChat.getId());
        otherUser.getChats().add(savedChat.getId());

        userRepository.save(user);
        userRepository.save(otherUser);

        return ResponseEntity.ok(savedChat.getId());
    }

    @GetMapping("/getChat/{chatId}")
    @Transactional
    public ResponseEntity<ChatDto> getChat(
            @PathVariable Integer chatId,
            @RequestParam Integer userId
    ) {

        Chat chat = chatRepository.findById(chatId)
                .orElse(null);

        if (chat == null) {
            return ResponseEntity.notFound().build();
        }

        if (!chat.getMembers().contains(userId)) {
            return ResponseEntity.status(403).build();
        }

        Integer otherUserId = chat.getMembers()
                .stream()
                .filter(id -> !id.equals(userId))
                .findFirst()
                .orElse(null);

        if (otherUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        User otherUser = userRepository.findById(otherUserId)
                .orElse(null);

        if (otherUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                ChatDto.builder()
                        .id(chat.getId())
                        .title(otherUser.getNickname())
                        .avatar(otherUser.getAvatar())
                        .build()
        );
    }

    @GetMapping("/getChats/{userId}")
    @Transactional
    public ResponseEntity<List<ChatDto>> getChats(
            @PathVariable Integer userId
    ) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<ChatDto> chats = user.getChats()
                .stream()
                .map(chatId -> {

                    Chat chat = chatRepository.findById(chatId)
                            .orElse(null);

                    if (chat == null) {
                        return null;
                    }

                    Integer otherUserId = chat.getMembers()
                            .stream()
                            .filter(id -> !id.equals(userId))
                            .findFirst()
                            .orElse(null);

                    if (otherUserId == null) {
                        return null;
                    }

                    User otherUser = userRepository.findById(otherUserId)
                            .orElse(null);

                    if (otherUser == null) {
                        return null;
                    }

                    return ChatDto.builder()
                            .id(chat.getId())
                            .title(otherUser.getNickname())
                            .avatar(otherUser.getAvatar())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        return ResponseEntity.ok(chats);
    }

    @DeleteMapping("/deleteChat/{chatId}")
    @Transactional
    public ResponseEntity<Void> deleteChat(
            @PathVariable Integer chatId
    ) {
        Chat chat = chatRepository.findById(chatId)
                .orElse(null);

        if (chat == null) {
            return ResponseEntity.notFound().build();
        }

        chat.getMembers().stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(user -> {
                    user.getChats().removeIf(id -> id.equals(chatId));
                    userRepository.save(user);
                });

        messageRepository.deleteByChat_Id(chat.getId());
        chatRepository.delete(chat);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/allChats")
    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }
}