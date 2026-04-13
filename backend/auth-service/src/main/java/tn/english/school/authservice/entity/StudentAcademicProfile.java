package tn.english.school.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_academic_profile")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentAcademicProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser student;

    /** Age of the student (15–22) */
    private int age;

    /** First period grade (0–20) */
    private int g1;

    /** Second period grade (0–20) */
    private int g2;

    /** Number of past class failures (0–3) */
    private int failures;

    /** Number of school absences (0–30) */
    private int absences;

    /**
     * Weekly study time:
     * 1 = <2h, 2 = 2–5h, 3 = 5–10h, 4 = >10h
     */
    private int studytime;
}