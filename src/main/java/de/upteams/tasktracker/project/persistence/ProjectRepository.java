package de.upteams.tasktracker.project.persistence;

import de.upteams.tasktracker.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;



@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByOwner(AppUser owner);

    Optional<Project> findByIdAndOwner(UUID id, AppUser owner);
}
