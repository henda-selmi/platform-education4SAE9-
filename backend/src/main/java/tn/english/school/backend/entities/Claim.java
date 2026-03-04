package tn.english.school.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
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
import tn.english.school.backend.enums.ClaimStatus;
import tn.english.school.backend.enums.ClaimType;

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
  @JsonIgnoreProperties({"claims", "retakeRequests", "password", "email", "firstName", "lastName"})
  private User student;

  @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
  private RetakeRequest retakeRequest;

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