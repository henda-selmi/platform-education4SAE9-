package tn.english.school.retakeservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClaimDTO {
    private Long id;
    private String subject;
    private String description;
    private String status;   // ClaimStatus as String
    private String type;     // ClaimType as String
    private Long retakeRequestId;
    private StudentDTO student;

    @Data
    @NoArgsConstructor
    public static class StudentDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
    }
}