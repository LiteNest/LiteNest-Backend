package container.desktop.containerdesktopbackend.repository;

import container.desktop.api.repository.ContainerRepository;
import container.desktop.containerdesktopbackend.entity.BackendContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("container_repo")
public interface BackendContainerRepository
        extends JpaRepository<BackendContainer, String>, ContainerRepository<BackendContainer> {

}
