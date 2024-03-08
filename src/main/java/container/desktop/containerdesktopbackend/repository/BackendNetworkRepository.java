package container.desktop.containerdesktopbackend.repository;

import container.desktop.api.repository.NetworkRepository;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackendNetworkRepository
        extends JpaRepository<BackendNetwork, String>, NetworkRepository<BackendNetwork> {
}
