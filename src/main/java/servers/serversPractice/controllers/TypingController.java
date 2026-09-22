package servers.serversPractice.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import servers.serversPractice.dtos.TypingDto;

@Controller
public class TypingController {

    private final SimpMessagingTemplate messagingTemplate;

    public TypingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/typing")
    public void typing(TypingDto typingDto) {

        messagingTemplate.convertAndSend(
                "/topic/chat/" + typingDto.chatId() + "/typing",
                typingDto
        );
    }
}