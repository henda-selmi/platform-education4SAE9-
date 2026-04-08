package tn.english.school.claimservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id;  // manually set to match auth-service user ID

    private String firstName;
    private String lastName;
    private String email;

    @JsonIgnore
    private String password;

    @JsonIgnore
    @OneToMany(mappedBy = "student")
    private List<Claim> claims;
}