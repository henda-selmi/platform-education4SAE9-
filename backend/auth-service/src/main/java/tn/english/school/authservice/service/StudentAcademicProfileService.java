package tn.english.school.authservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.english.school.authservice.entity.AppUser;
import tn.english.school.authservice.entity.StudentAcademicProfile;
import tn.english.school.authservice.enums.Role;
import tn.english.school.authservice.repository.StudentAcademicProfileRepository;
import tn.english.school.authservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAcademicProfileService {

    private final StudentAcademicProfileRepository profileRepository;
    private final UserRepository userRepository;

    /** Auto-generate fictional academic profiles for every STUDENT on startup. */
    @PostConstruct
    public void seedProfiles() {
        List<AppUser> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .toList();

        Random rng = new Random(42);
        int seeded = 0;

        for (AppUser student : students) {
            if (profileRepository.existsByStudentId(student.getId())) continue;

            int g1 = 6 + rng.nextInt(13);          // 6–18
            int g2 = Math.max(0, g1 + rng.nextInt(5) - 2); // g2 ≈ g1 ± 2
            g2 = Math.min(g2, 20);

            StudentAcademicProfile profile = StudentAcademicProfile.builder()
                    .student(student)
                    .age(16 + rng.nextInt(6))        // 16–21
                    .g1(g1)
                    .g2(g2)
                    .failures(weightedFailures(rng))
                    .absences(rng.nextInt(20))        // 0–19
                    .studytime(1 + rng.nextInt(4))   // 1–4
                    .build();

            profileRepository.save(profile);
            seeded++;
        }

        if (seeded > 0) {
            log.info("[AcademicProfile] Seeded {} fictional profile(s) for students.", seeded);
        }
    }

    public Optional<StudentAcademicProfile> findByStudentId(Long studentId) {
        return profileRepository.findByStudentId(studentId);
    }

    /** Most students have 0 failures; few have 1–3. */
    private int weightedFailures(Random rng) {
        int r = rng.nextInt(100);
        if (r < 65) return 0;
        if (r < 84) return 1;
        if (r < 94) return 2;
        return 3;
    }
}