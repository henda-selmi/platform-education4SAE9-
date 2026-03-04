package tn.esprit.backend.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EnableDiscoveryClient
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // mappedBy indique que le champ "module" dans la classe Course possède la clé étrangère
    // orphanRemoval = true permet de supprimer un cours si on l'enlève de la liste du module
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();

    // --- Helpers pour maintenir la cohérence bidirectionnelle ---
    public void addCourse(Course course) {
        courses.add(course);
        course.setModule(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setModule(null);
    }

}
