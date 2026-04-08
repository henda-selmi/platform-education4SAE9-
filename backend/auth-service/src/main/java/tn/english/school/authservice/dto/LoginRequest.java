package tn.english.school.authservice.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;   // frontend sends "username" field
    private String email;      // fallback
    private String password;
}
