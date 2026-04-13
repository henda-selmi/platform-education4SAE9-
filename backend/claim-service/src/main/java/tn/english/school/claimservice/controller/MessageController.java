package tn.english.school.claimservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.english.school.claimservice.entity.Message;
import tn.english.school.claimservice.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/claims/{claimId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long claimId) {
        return ResponseEntity.ok(messageService.getMessages(claimId));
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(@PathVariable Long claimId,
                                               @RequestBody Message message) {
        return ResponseEntity.ok(messageService.sendMessage(claimId, message));
    }
}
