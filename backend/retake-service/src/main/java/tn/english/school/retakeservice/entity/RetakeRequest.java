package tn.english.school.retakeservice.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import tn.english.school.retakeservice.enums.DocumentStatus;
import tn.english.school.retakeservice.enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetakeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;
    private String reason;
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    // References from other services — stored as plain IDs, no JPA joins across services
    private Long studentId;
    private Long claimId;

    private String attachmentPath;

    @Enumerated(EnumType.STRING)
    private DocumentStatus documentStatus;

    private String documentRejectionReason;

    @PrePersist
    protected void onCreate() {
        if (this.requestDate == null) {
            this.requestDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
        if (this.documentStatus == null && this.attachmentPath != null) {
            this.documentStatus = DocumentStatus.PENDING_REVIEW;
        }
    }
}