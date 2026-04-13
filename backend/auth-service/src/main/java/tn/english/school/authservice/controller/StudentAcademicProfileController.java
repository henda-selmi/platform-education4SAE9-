package tn.english.school.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.english.school.authservice.entity.StudentAcademicProfile;
import tn.english.school.authservice.service.StudentAcademicProfileService;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentAcademicProfileController {

    private final StudentAcademicProfileService profileService;

    /**
     * GET /api/students/{userId}/academic-profile
     * Returns the academic profile of a student — used by the ML service call in the frontend.
     */
    @GetMapping("/{userId}/academic-profile")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        return profileService.findByStudentId(userId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "userId",    userId,
                        "age",       p.getAge(),
                        "g1",        p.getG1(),
                        "g2",        p.getG2(),
                        "failures",  p.getFailures(),
                        "absences",  p.getAbsences(),
                        "studytime", p.getStudytime()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}