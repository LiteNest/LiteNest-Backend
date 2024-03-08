package container.desktop.containerdesktopbackend.repository;

import container.desktop.api.repository.UserRepository;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("user_repo")
public interface BackendUserRepository
        extends JpaRepository<BackendUser, Long>, UserRepository<BackendUser> {

}
