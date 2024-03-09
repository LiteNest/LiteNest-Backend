package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import container.desktop.api.entity.Container;
import container.desktop.api.repository.ContainerRepository;
import container.desktop.api.repository.ImageRepository;
import container.desktop.api.repository.NetworkRepository;
import container.desktop.api.repository.UserRepository;
import container.desktop.api.service.ContainerService;
import container.desktop.api.service.PortService;
import container.desktop.containerdesktopbackend.entity.BackendContainer;
import container.desktop.containerdesktopbackend.entity.BackendImage;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("container_service")
public class BackendContainerService implements ContainerService<BackendContainer> {

    private final ContainerRepository<BackendContainer> containerContainerRepository;
    private final ImageRepository<BackendImage> imageImageRepository;
    private final NetworkRepository<BackendNetwork> networkRepository;
    private final UserRepository<BackendUser> userRepository;
    private final DockerClient client;
    private final PortService portService;

    public BackendContainerService(
            @Qualifier("container_repo") ContainerRepository<BackendContainer> containerContainerRepository,
            ImageRepository<BackendImage> imageImageRepository,
            NetworkRepository<BackendNetwork> networkRepository,
            @Qualifier("user_repo") UserRepository<BackendUser> userRepository,
            DockerClient client,
            @Qualifier("port_service") PortService portService) {
        this.containerContainerRepository = containerContainerRepository;
        this.imageImageRepository = imageImageRepository;
        this.networkRepository = networkRepository;
        this.userRepository = userRepository;
        this.client = client;
        this.portService = portService;
    }

    @Override
    public List<? extends Container> list() {
        return containerContainerRepository.findAll();
    }

    @Nullable
    @Override
    public Container findById(String id) {
        Optional<BackendContainer> optional = containerContainerRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public List<? extends Container> findByIds(List<String> containerIds) {
        return containerContainerRepository.findByIdIn(containerIds);
    }

    @Override
    public String create(String name,
                         String imageId,
                         String networkId,
                         Integer rootDisk,
                         Integer vcpu,
                         Integer RAM,
                         String command,
                         @NotNull String username) {
        if (command.isBlank()) {
            command = "tail -f /dev/null";
        }
        Optional<BackendImage> imageOptional = imageImageRepository.findById(imageId);
        assert imageOptional.isPresent();
        Integer port = imageOptional.get().getRemoteDesktopPort();
        Optional<BackendNetwork> networkOptional = networkRepository.findById(networkId);
        assert networkOptional.isPresent();
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withDiskQuota(rootDisk.longValue() * 1024 * 1024 * 1024)
                .withCpuCount(vcpu.longValue())
                .withMemory(RAM.longValue() * 1024 * 1024)
                .withNetworkMode(networkOptional.get().getName());
        Integer host_port = portService.randomPort();
        if (port != null) {
            hostConfig.withPortBindings(
                    PortBinding.parse( +host_port + ":" + port)
            );
        }
        CreateContainerCmd createContainerCmd = client.createContainerCmd(imageId)
                .withCmd(Arrays.stream(command.split("\\s+")).toList())
                .withHostConfig(hostConfig);
        if (name != null){
            createContainerCmd.withName(name);
        }
        String id = createContainerCmd.exec().getId();
        containerContainerRepository.saveAndFlush(BackendContainer.builder()
                        .id(id)
                        .RAM(RAM)
                        .rootDisk(rootDisk)
                        .Vcpus(vcpu)
                        .imageId(imageId)
                        .networkIds(List.of(networkId))
                .build());
        Optional<BackendUser> userOptional = userRepository.findByUsername(username);
        assert userOptional.isPresent();
        BackendUser backendUser = userOptional.get();
        backendUser.addContainer(id);
        userRepository.saveAndFlush(backendUser);
        return id;
    }

    @Override
    public String create(String imageId,
                         String networkId,
                         Integer rootDisk,
                         Integer vcpu,
                         Integer RAM,
                         String command,
                         @NotNull String username) {
        return create(null, imageId, networkId, rootDisk, vcpu, RAM, command, username);
    }

    @Override
    public void attachTo(String networkId) {

    }

    @Override
    public void update(BackendContainer entity) {

    }
}
