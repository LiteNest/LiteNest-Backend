package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import container.desktop.api.entity.Container;
import container.desktop.api.entity.User;
import container.desktop.api.exception.ContainerCreationException;
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
import container.desktop.containerdesktopbackend.event.ContainerCreatedEvent;
import container.desktop.containerdesktopbackend.event.ContainerRemovedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("container_service")
public class BackendContainerService implements ContainerService<BackendContainer> {

    private static final Logger log = LoggerFactory.getLogger("容器服务");
    private final ContainerRepository<BackendContainer> containerRepository;
    private final ImageRepository<BackendImage> imageImageRepository;
    private final NetworkRepository<BackendNetwork> networkRepository;
    private final UserRepository<BackendUser> userRepository;
    private final DockerClient client;
    private final PortService portService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public BackendContainerService(
            @Qualifier("container_repo") ContainerRepository<BackendContainer> containerRepository,
            ImageRepository<BackendImage> imageImageRepository,
            NetworkRepository<BackendNetwork> networkRepository,
            @Qualifier("user_repo") UserRepository<BackendUser> userRepository,
            DockerClient client,
            @Qualifier("port_service") PortService portService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.containerRepository = containerRepository;
        this.imageImageRepository = imageImageRepository;
        this.networkRepository = networkRepository;
        this.userRepository = userRepository;
        this.client = client;
        this.portService = portService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public List<? extends Container> list() {
        return containerRepository.findAll();
    }

    @Nullable
    @Override
    public Container findById(String id) {
        Optional<BackendContainer> optional = containerRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public List<? extends Container> findByIds(List<String> containerIds) {
        return containerRepository.findByIdIn(containerIds);
    }

    @Override
    public String create(String name,
                         String customName,
                         String imageId,
                         String networkId,
                         Integer rootDisk,
                         Integer vcpu,
                         Integer RAM,
                         String command,
                         @NotNull String username) throws ContainerCreationException {
        if (command.isBlank()) {
            command = "tail -f /dev/null";
        }
        Optional<BackendUser> userOptional = userRepository.findByUsername(username);
        assert userOptional.isPresent();
        Optional<BackendImage> imageOptional = imageImageRepository.findById(imageId);
        assert imageOptional.isPresent();
        if (!userOptional.get().hasRole(User.Role.ADMIN) && !imageOptional.get().isPublic()) {
            throw new ContainerCreationException("使用了非公开镜像", ContainerCreationException.Reason.USING_NON_PUBLIC_IMAGE);
        }
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
        BackendContainer container = BackendContainer.builder()
                .id(id)
                .customName(customName)
                .RAM(RAM)
                .rootDisk(rootDisk)
                .Vcpus(vcpu)
                .imageId(imageId)
                .networkIds(List.of(networkId))
                .powerStatus(Container.PowerStatus.POWER_OFF)
                .ownerId(userOptional.get().getId())
                .build();
        containerRepository.saveAndFlush(container);

        BackendUser backendUser = userOptional.get();
        backendUser.addContainer(id);
        userRepository.saveAndFlush(backendUser);
        applicationEventPublisher.publishEvent(new ContainerCreatedEvent(this, container, userOptional.get()));
        return id;
    }

    @Override
    public String create(String customName,
                         String imageId,
                         String networkId,
                         Integer rootDisk,
                         Integer vcpu,
                         Integer RAM,
                         String command,
                         @NotNull String username) throws ContainerCreationException{
        return create(null, customName, imageId, networkId, rootDisk, vcpu, RAM, command, username);
    }

    @Override
    public void delete(String containerId) {
        client.killContainerCmd(containerId);
        client.removeContainerCmd(containerId).withForce(true).exec();
        applicationEventPublisher.publishEvent(new ContainerRemovedEvent(this, findById(containerId)));
        containerRepository.deleteAllByIdInBatch(List.of(containerId));
    }

    @Override
    public void attachTo(String networkId) {

    }

    @Override
    public Integer getMaxVCPUs() {
        return Runtime.getRuntime().availableProcessors();
    }

    public void start(Container container) {
        String containerId = container.getId();
        log.info("启动容器：{}", containerId);
        container.setPowerStatus(Container.PowerStatus.STARTING);
        containerRepository.save((BackendContainer) container);
        client.startContainerCmd(containerId).exec();
        log.info("容器{}已启动", containerId);
        container.setPowerStatus(Container.PowerStatus.ACTIVE);
        containerRepository.saveAndFlush((BackendContainer) container);
    }

    public void stop(Container container) {
        String containerId = container.getId();
        log.info("开始关闭容器：{}", containerId);
        container.setPowerStatus(Container.PowerStatus.STOPPING);
        containerRepository.saveAndFlush((BackendContainer) container);
        client.stopContainerCmd(containerId).exec();
        log.info("容器{}已关闭", containerId);
        container.setPowerStatus(Container.PowerStatus.POWER_OFF);
        containerRepository.saveAndFlush((BackendContainer) container);
    }

    @Override
    public void update(BackendContainer entity) {
        Optional<BackendContainer> repoContainer = containerRepository.findById(entity.getId());
        assert repoContainer.isPresent();
        BackendContainer container = repoContainer.get();
        if (container.getPowerStatus() != entity.getPowerStatus()) {
            if (entity.getPowerStatus() == Container.PowerStatus.ACTIVE) {
                start(entity);
            } else if (entity.getPowerStatus() == Container.PowerStatus.POWER_OFF) {
                stop(entity);
            } else {
                // 只能将容器状态设置为关机或运行，不能将状态设为“关闭中”或“开启中”
                log.warn("不能将容器{}设置为状态{}", container.getId(), entity.getPowerStatus());
            }

            containerRepository.saveAndFlush(entity);
        }
    }
}
