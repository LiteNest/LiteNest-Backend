package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import container.desktop.api.entity.Volume;
import container.desktop.api.exception.UpdatingException;
import container.desktop.api.exception.VolumeInUseException;
import container.desktop.api.repository.UserRepository;
import container.desktop.api.repository.VolumeRepository;
import container.desktop.api.service.VolumeService;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.entity.BackendVolume;
import container.desktop.containerdesktopbackend.event.VolumeCreatedEvent;
import container.desktop.containerdesktopbackend.event.VolumeRemovedEvent;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("volume_service")
public class BackendVolumeService implements VolumeService<BackendVolume> {

    private final VolumeRepository<BackendVolume> volumeRepository;
    private final DockerClient client;
    private final ApplicationEventPublisher applicationEventPublisher;

    public BackendVolumeService(VolumeRepository<BackendVolume> volumeRepository, DockerClient client, ApplicationEventPublisher applicationEventPublisher) {
        this.volumeRepository = volumeRepository;
        this.client = client;
        this.applicationEventPublisher = applicationEventPublisher;
    }


    @Override
    public List<? extends Volume> list() {
        return volumeRepository.findAll();
    }

    @Nullable
    @Override
    public Volume findById(String id) {
        return volumeRepository.findById(id).orElse(null);
    }

    @Override
    public List<? extends Volume> findByIds(List<String> ids) {
        return volumeRepository.findByIdIn(ids);
    }

    @Override
    public void deleteById(String id) {
        Optional<BackendVolume> optional = volumeRepository.findById(id);
        assert optional.isPresent();
        volumeRepository.delete(optional.get());
    }

    @Override
    public String create(Integer size, Long userId) {
        return create(size, null, userId);
    }

    @Override
    public String create(Integer size, String customName, Long userId) {
        String name = client.createVolumeCmd()
                .withDriver("loopback")
                .withDriverOpts(Map.of(
                        "size", size + "g",
                        "sparse", "true",
                        "fs", "ext4"
                ))
                .exec().getName();
        BackendVolume volume = BackendVolume.builder()
                .customName(customName)
                .ownerId(userId)
                .id(name)
                .size(size)
                .build();
        volumeRepository.saveAndFlush(volume);
        applicationEventPublisher.publishEvent(new VolumeCreatedEvent(this, userId, volume));
        return name;
    }

    @Override
    public void delete(String id) throws VolumeInUseException {
        Optional<BackendVolume> optional = volumeRepository.findById(id);
        assert optional.isPresent();
        BackendVolume backendVolume = optional.get();
        if (backendVolume.getContainerIds() == null || backendVolume.getContainerIds().isEmpty()) {
            client.removeVolumeCmd(id).exec();
            volumeRepository.delete(backendVolume);
            applicationEventPublisher.publishEvent(new VolumeRemovedEvent(this, backendVolume));
        } else {
            throw new VolumeInUseException(backendVolume.getContainerIds());
        }

    }

    @Override
    public void update(BackendVolume entity) throws UpdatingException {
        volumeRepository.saveAndFlush(entity);
    }
}
