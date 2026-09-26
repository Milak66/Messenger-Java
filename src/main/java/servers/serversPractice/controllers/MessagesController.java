package servers.serversPractice.controllers;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import servers.serversPractice.dtos.AddMessage;
import servers.serversPractice.dtos.MessageDto;
import servers.serversPractice.dtos.UserDto;
import servers.serversPractice.entities.Chat;
import servers.serversPractice.entities.Message;
import servers.serversPractice.entities.User;
import servers.serversPractice.repositories.ChatRepository;
import servers.serversPractice.repositories.MessageRepository;
import servers.serversPractice.repositories.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessagesController {
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessagesController(
            UserRepository userRepository,
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/getMessages/{chatId}")
    @Transactional
    public ResponseEntity<List<MessageDto>> getMessages(
            @PathVariable Integer chatId
    ) {
        List<MessageDto> messages = messageRepository
                .findByChat_Id(chatId)
                .stream()
                .map(message -> MessageDto.builder()
                        .id(message.getId())
                        .text(message.getText())
                        .sendTime(message.getSendTime().toString())
                        .sender(UserDto.builder()
                                .id(message.getSender().getId())
                                .username(message.getSender().getUsername())
                                .nickname(message.getSender().getNickname())
                                .avatar(message.getSender().getAvatar())
                                .language(message.getSender().getLanguage())
                                .chats(message.getSender().getChats())
                                .build())
                        .build())
                .toList();

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/addMessage")
    public ResponseEntity<MessageDto> addMessage(
            @RequestBody AddMessage addMessage
    ) {
        User user = userRepository
                .findById(addMessage.sender())
                .orElse(null);

        Chat chat = chatRepository
                .findById(addMessage.currentChatId())
                .orElse(null);

        Message message = Message.builder()
                .text(addMessage.messageText())
                .sender(user)
                .chat(chat)
                .build();

        messageRepository.save(message);

        MessageDto messageDto = MessageDto.builder()
                .id(message.getId())
                .text(message.getText())
                .sendTime(message.getSendTime().toString())
                .sender(UserDto.builder()
                        .id(message.getSender().getId())
                        .username(message.getSender().getUsername())
                        .nickname(message.getSender().getNickname())
                        .avatar(message.getSender().getAvatar())
                        .language(message.getSender().getLanguage())
                        .chats(message.getSender().getChats())
                        .build())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chat/" + chat.getId(),
                messageDto
        );

        return ResponseEntity.ok(messageDto);
    }

    @GetMapping("/allMessages")
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}