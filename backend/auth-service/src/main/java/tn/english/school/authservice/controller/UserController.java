package tn.english.school.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.english.school.authservice.entity.AppUser;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    /** GET /user — returns current user info extracted from the JWT */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(Map.of(
                "id",    user.getId(),
                "name",  user.getFirstName() + " " + user.getLastName(),
                "email", user.getEmail(),
                "roles", List.of(user.getRole().name()),
                "permissions", permissionsForRole(user.getRole().name())
        ));
    }

    /** GET /user/menu — returns a role-specific sidebar menu */
    @GetMapping("/user/menu")
    public ResponseEntity<Map<String, Object>> getMenu(@AuthenticationPrincipal AppUser user) {
        String role = user.getRole().name();

        List<Map<String, Object>> claimsChildren = role.equals("ADMIN")
                ? List.of(
                    Map.of("route", "",                 "name", "all-claims",        "type", "link", "icon", "list"),
                    Map.of("route", "retake-requests",  "name", "retake-requests",   "type", "link", "icon", "replay")
                  )
                : List.of(
                    Map.of("route", "", "name", "all-claims", "type", "link", "icon", "list")
                  );

        List<Map<String, Object>> menu = List.of(
                Map.of("route", "dashboard", "name", "dashboard", "type", "link", "icon", "dashboard"),
                Map.of(
                        "route",    "claims",
                        "name",     "claims",
                        "type",     "sub",
                        "icon",     "assignment",
                        "children", claimsChildren
                )
        );

        return ResponseEntity.ok(Map.of("menu", menu));
    }

    private List<String> permissionsForRole(String role) {
        if ("ADMIN".equals(role)) {
            return List.of("canRead", "canAdd", "canEdit", "canDelete");
        }
        return List.of("canRead", "canAdd");
    }
}
