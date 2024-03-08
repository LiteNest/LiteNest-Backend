package container.desktop.containerdesktopbackend.repository;

import container.desktop.api.repository.ImageRepository;
import container.desktop.containerdesktopbackend.entity.BackendImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackendImageRepository
        extends JpaRepository<BackendImage, String>, ImageRepository<BackendImage> {
}
