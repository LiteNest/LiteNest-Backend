package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Volume;
import container.desktop.api.entity.Container;
import container.desktop.api.entity.User;
import container.desktop.api.exception.ContainerCreationException;
import container.desktop.api.exception.ResourceNotFoundException;
import container.desktop.api.repository.*;
import container.desktop.api.service.ContainerService;
import container.desktop.api.service.PortService;
import container.desktop.containerdesktopbackend.entity.*;
import container.desktop.containerdesktopbackend.event.ContainerCreatedEvent;
import container.desktop.containerdesktopbackend.event.ContainerRemovedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("container_service")
public class BackendContainerService implements ContainerService<BackendContainer> {

    private static final Logger log = LoggerFactory.getLogger("容器服务");
    private final ContainerRepository<BackendContainer> containerRepository;
    private final ImageRepository<BackendImage> imageImageRepository;
    private final NetworkRepository<BackendNetwork> networkRepository;
    private final UserRepository<BackendUser> userRepository;
    private final VolumeRepository<BackendVolume> volumeRepository;
    private final DockerClient client;
    private final PortService portService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public BackendContainerService(
            @Qualifier("container_repo") ContainerRepository<BackendContainer> containerRepository,
            ImageRepository<BackendImage> imageImageRepository,
            NetworkRepository<BackendNetwork> networkRepository,
            VolumeRepository<BackendVolume> volumeRepository,
            @Qualifier("user_repo") UserRepository<BackendUser> userRepository,
            DockerClient client,
            @Qualifier("port_service") PortService portService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.containerRepository = containerRepository;
        this.imageImageRepository = imageImageRepository;
        this.networkRepository = networkRepository;
        this.userRepository = userRepository;
        this.volumeRepository = volumeRepository;
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
        return create(name, customName, imageId, networkId, rootDisk, vcpu, RAM, command, username, List.of());
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
        return create(null, customName, imageId, networkId, rootDisk, vcpu, RAM, command, username, List.of());
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
                         @NotNull String username,
                         List<container.desktop.api.entity.Volume.VolumeBinding> volumeIds) throws ContainerCreationException {
        return create(name, customName, imageId, networkId, rootDisk, vcpu, RAM, command, username, Map.of(), volumeIds);
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
                         @NotNull String username,
                         Map<String, String> env,
                         List<container.desktop.api.entity.Volume.VolumeBinding> volumeIds) throws ContainerCreationException {
//        if (command.isBlank()) {
//            command = "tail -f /dev/null";
//        }
        Optional<BackendUser> userOptional = userRepository.findByUsername(username);
        assert userOptional.isPresent();
        Optional<BackendImage> imageOptional = imageImageRepository.findById(imageId);
        if (imageOptional.isEmpty()) {
            log.error("收到创建容器的请求，但是指定的镜像{}不存在", imageId);
            throw new ResourceNotFoundException("收到创建容器的请求，但是指定的镜像" + imageId + "不存在");
        }
        if (!userOptional.get().hasRole(User.Role.ADMIN) && !imageOptional.get().isPublic()) {
            throw new ContainerCreationException("使用了非公开镜像", ContainerCreationException.Reason.USING_NON_PUBLIC_IMAGE);
        }
        if (vcpu < imageOptional.get().getMinimumVcpus() || RAM < imageOptional.get().getMinimumRAM() || rootDisk < imageOptional.get().getMinimumRootDisk()) {
            throw new ContainerCreationException("不符合最低配置要求", ContainerCreationException.Reason.INSUFFICIENT_MINIMUM_REQUIREMENTS);
        }

        Integer port = imageOptional.get().getRemoteDesktopPort();
        Optional<BackendNetwork> networkOptional = networkRepository.findById(networkId);
        assert networkOptional.isPresent();
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withDiskQuota(rootDisk.longValue() * 1024 * 1024 * 1024)
                .withVolumeDriver("loopback")
                .withCpuCount(vcpu.longValue())
                .withMemory(RAM.longValue() * 1024 * 1024)
                .withBinds(volumeIds.stream().map(
                        volumeBinding -> new Bind(
                                volumeBinding.getVolumeId(),
                                new Volume(volumeBinding.getMountPath())
                        )
                ).toList())
                .withNetworkMode(networkOptional.get().getName());
        Integer host_port = null;
        if (port != null) {
            // 循环找端口号，避免冲突
            while (true){
                host_port = portService.randomPort();
                // 如果找不到与当前随机分配的端口相同的端口号，则说明该端口号是可用的
                if (containerRepository.findByPort(host_port).isEmpty()) {
                    hostConfig.withPortBindings(
                            PortBinding.parse( host_port + ":" + port)
                    );
                    break;
                }
            }
        }
        CreateContainerCmd createContainerCmd = client.createContainerCmd(imageId)
                .withEnv(env.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toList())
                .withHostConfig(hostConfig);
        if (command != null) {
            if (!command.isBlank()) {
                createContainerCmd.withCmd(Arrays.stream(command.split("\\s+")).toList());
            }
        }
        if (name != null){
            createContainerCmd.withName(name);
        }
        String id = createContainerCmd.exec().getId();
        // 生成容器实体存入数据库
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
                .port(host_port)
                .build();
        // 为容器实体新增数据卷挂载配置
        container.addDataVolumeIds(volumeIds.stream().map(volumeBinding -> volumeBinding.getVolumeId()).toList());
        containerRepository.saveAndFlush(container);
        List<BackendVolume> backendVolumes = new LinkedList<>();
        volumeIds.forEach(volumeBinding -> {
            Optional<BackendVolume> optional = volumeRepository.findById(volumeBinding.getVolumeId());
            assert optional.isPresent();
            BackendVolume backendVolume = optional.get();
            backendVolume.addContainerId(id);
            backendVolumes.add(backendVolume);
        });
        volumeRepository.saveAllAndFlush(backendVolumes);
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
                         @NotNull String username,
                         List<container.desktop.api.entity.Volume.VolumeBinding> volumeIds) throws ContainerCreationException {
        return create(null, customName, imageId, networkId, rootDisk, vcpu, RAM, command, username, volumeIds);
    }

    @Override
    public String create(String customName,
                         String imageId,
                         String networkId,
                         Integer rootDisk,
                         Integer vcpu,
                         Integer RAM,
                         String command,
                         @NotNull String username,
                         Map<String, String> env,
                         List<container.desktop.api.entity.Volume.VolumeBinding> volumeIds) throws ContainerCreationException {
        return create(null, customName, imageId, networkId, rootDisk, vcpu, RAM, command, username, env, volumeIds);
    }

    @Override
    public void delete(String containerId) {
        try {
            client.killContainerCmd(containerId);
            client.removeContainerCmd(containerId).withForce(true).exec();
        } catch (NotFoundException ignored) {

        } finally {
            applicationEventPublisher.publishEvent(new ContainerRemovedEvent(this, findById(containerId)));
            containerRepository.deleteAllByIdInBatch(List.of(containerId));
        }


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
