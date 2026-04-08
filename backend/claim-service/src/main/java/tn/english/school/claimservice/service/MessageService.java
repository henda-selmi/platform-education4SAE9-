package tn.english.school.claimservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.english.school.claimservice.entity.Message;
import tn.english.school.claimservice.repository.MessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ClaimService claimService;

    public List<Message> getMessages(Long claimId) {
        // verify claim exists
        claimService.getClaimById(claimId);
        return messageRepository.findByClaimIdOrderByCreatedAtAsc(claimId);
    }

    public Message sendMessage(Long claimId, Message message) {
        // verify claim exists
        claimService.getClaimById(claimId);
        message.setClaimId(claimId);
        return messageRepository.save(message);
    }
}
