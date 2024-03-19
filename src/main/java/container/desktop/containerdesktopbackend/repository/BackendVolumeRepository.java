package container.desktop.containerdesktopbackend.repository;

import container.desktop.api.repository.VolumeRepository;
import container.desktop.containerdesktopbackend.entity.BackendVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackendVolumeRepository
        extends JpaRepository<BackendVolume, String>, VolumeRepository<BackendVolume> {

}
