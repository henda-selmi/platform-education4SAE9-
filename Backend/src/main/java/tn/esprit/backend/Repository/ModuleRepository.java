package tn.esprit.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.backend.Entity.Module;


@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

}
