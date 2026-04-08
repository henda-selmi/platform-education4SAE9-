package tn.english.school.claimservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import tn.english.school.claimservice.enums.ClaimStatus;
import tn.english.school.claimservice.enums.ClaimType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String description;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    @Enumerated(EnumType.STRING)
    private ClaimType type;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"claims", "password"})
    private User student;

    // Stores the ID of the linked RetakeRequest from retake-service (no JPA cross-service join)
    private Long retakeRequestId;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ClaimStatus.OPEN;
        }
    }

    @JsonProperty("studentId")
    public void setStudentById(Long studentId) {
        if (studentId != null) {
            this.student = new User();
            this.student.setId(studentId);
        }
    }
}