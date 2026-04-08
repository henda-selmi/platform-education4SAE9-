package tn.english.school.authservice.dto;

import lombok.Data;
import tn.english.school.authservice.enums.Role;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role = Role.STUDENT;
}
