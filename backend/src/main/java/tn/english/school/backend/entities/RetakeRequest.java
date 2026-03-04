package tn.english.school.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.english.school.backend.enums.RequestStatus;

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

  @ManyToOne
  @JoinColumn(name = "student_id")
  @JsonIgnoreProperties({"claims", "retakeRequests", "password", "email", "firstName", "lastName"})
  private User student;

  @OneToOne
  @JoinColumn(name = "claim_id", referencedColumnName = "id")
  @JsonIgnoreProperties({"retakeRequest", "student"})
  private Claim claim;

  private String attachmentPath;

  @PrePersist
  protected void onCreate() {
    if (this.requestDate == null) {
      this.requestDate = LocalDateTime.now();
    }
    if (this.status == null) {
      this.status = RequestStatus.PENDING;
    }
  }
}