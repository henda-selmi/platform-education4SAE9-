package tn.esprit.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.backend.Entity.Course;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

}
