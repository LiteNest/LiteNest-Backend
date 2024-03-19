package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerNetwork;
import container.desktop.api.entity.Container;
import container.desktop.api.entity.Network;
import container.desktop.api.repository.ContainerRepository;
import container.desktop.api.repository.NetworkRepository;
import container.desktop.api.service.NetworkService;
import container.desktop.containerdesktopbackend.entity.BackendContainer;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("network_service")
public class BackendNetworkService implements NetworkService<BackendNetwork> {

    private static final Logger log = LoggerFactory.getLogger("网络服务");
    @Value("${container.auto-flush}")
    private boolean autoFlush;

    private final NetworkRepository<BackendNetwork> networkRepository;
    private final ContainerRepository<BackendContainer> containerRepository;
    private final DockerClient client;

    public BackendNetworkService(
            @Qualifier("network_repo") NetworkRepository<BackendNetwork> networkRepository,
            ContainerRepository<BackendContainer> containerRepository,
            DockerClient client) {
        this.networkRepository = networkRepository;
        this.containerRepository = containerRepository;
        this.client = client;
    }

    @PostConstruct
    public void init() {
        if (autoFlush) flush();
    }

    @Override
    public void flush(String networkId) {
        Optional<BackendNetwork> networkOptional = networkRepository.findById(networkId);
        if (networkOptional.isEmpty()) return;
        refresh(List.of(networkOptional.get()));
    }

    @Nullable
    @Override
    public Network findById(String id) {
        return networkRepository.findById(id).orElse(null);
    }

    @Override
    public List<? extends Network> findAll() {
        return networkRepository.findAll();
    }

    public void refresh(List<BackendNetwork> backendNetworks) {
        List<BackendContainer> backendContainers = containerRepository.findAll();
        for (BackendContainer container : backendContainers) {
            Set<Map.Entry<String, ContainerNetwork>> entries;
            try {
                entries = client.inspectContainerCmd(container.getId()).exec().getNetworkSettings().getNetworks().entrySet();
            } catch (NotFoundException e) {
                containerRepository.delete(container);
                continue;
            }
            Set<String> networkNames_in_database = backendNetworks.stream().map(Network::getName).collect(Collectors.toSet());
            // 筛选该容器加入的网络（该网络已在数据库中登记过），使用流式操作进行筛选
            List<Map.Entry<String, ContainerNetwork>> entries1 = entries.stream().filter(e -> networkNames_in_database.contains(e.getKey())).toList();
            entries1.forEach(entry -> {
                backendNetworks.forEach(backendNetwork -> {
                    if (Objects.equals(entry.getKey(), backendNetwork.getName())){
                        List<String> containerIds = backendNetwork.getContainerIds();
                        if (containerIds == null) {
                            containerIds = new ArrayList<>();
                        }
                        containerIds.add(container.getId());
                        backendNetwork.setContainerIds(containerIds);
                    }
                });
            });
        }
        networkRepository.saveAllAndFlush(backendNetworks);
    }

    @Override
    public void flush() {
        log.info("开始刷新网络数据库");
        long start = System.nanoTime();
        List<BackendNetwork> backendNetworks = new ArrayList<>();
        for (com.github.dockerjava.api.model.Network network : client.listNetworksCmd().exec()) {
            BackendNetwork.BackendNetworkBuilder backendNetworkBuilder = BackendNetwork.builder()
                    .id(network.getId())
                    .name(network.getName())
                    .networkDriver(Network.NetworkDriver.parse(network.getDriver()));
            if (network.getIpam().getConfig() != null) {
                backendNetworkBuilder.addr(network.getIpam().getConfig().getFirst().getSubnet());
            }
            backendNetworks.add(backendNetworkBuilder.build());
        }
        networkRepository.saveAllAndFlush(backendNetworks);
        refresh(backendNetworks);

        long end = System.nanoTime();
        log.info("网络数据库刷新完毕");
        log.info("网络数据库刷新用时{}ms", (end-start)/1.0e6);
    }

    @Override
    public List<? extends Network> list() {
        List<BackendNetwork> networks = networkRepository.findAll();
        for (BackendNetwork backendNetwork : networks) {
            for (String containerId : backendNetwork.getContainerIds()) {
                Optional<BackendContainer> optional = containerRepository.findById(containerId);
                assert optional.isPresent();
                backendNetwork.getContainers().add(optional.get());
            }
        }
        return networks;
    }

    @Override
    public Status deleteById(String id) {
        Optional<BackendNetwork> optional = networkRepository.findById(id);
        if (optional.isEmpty()) {
            return Status.NETWORK_NOT_EXISTS;
        }
        networkRepository.delete(optional.get());
        return Status.SUCCESS;
    }

    @Override
    public String create(String name, boolean attachable) {
        return client.createNetworkCmd().withName(name)
                .withAttachable(attachable)
                .exec().getId();
    }

    @Override
    public String create(String name, String address, boolean attachable) {
        return client.createNetworkCmd().withName(name)
                .withIpam(
                        new com.github.dockerjava.api.model.Network.Ipam()
                                .withConfig(
                                        new com.github.dockerjava.api.model.Network.Ipam.Config()
                                                .withSubnet(address)
                                )
                )
                .withAttachable(attachable)
                .exec().getId();
    }

    @Override
    public String create(String name, String address, Network.NetworkDriver driver, boolean attachable) {
        return client.createNetworkCmd().withName(name)
                .withIpam(
                        new com.github.dockerjava.api.model.Network.Ipam()
                                .withConfig(
                                        new com.github.dockerjava.api.model.Network.Ipam.Config()
                                                .withSubnet(address)
                                ).withDriver(driver.getName())
                )
                .withAttachable(attachable)
                .exec().getId();
    }

    @Override
    public String create(String name, Network.NetworkDriver driver, boolean attachable) {
        return client.createNetworkCmd().withName(name)
                .withIpam(
                        new com.github.dockerjava.api.model.Network.Ipam()
                                .withDriver(driver.getName())
                )
                .withAttachable(attachable)
                .exec().getId();
    }

    @Override
    public String create(String name) {
        return create(name, false);
    }

    @Override
    public String create(String name, String address) {
        return create(name, address, false);
    }

    @Override
    public String create(String name, String address, Network.NetworkDriver driver) {
        return create(name, address, driver, false);
    }

    @Override
    public String create(String name, Network.NetworkDriver driver) {
        return create(name, driver, false);
    }

    @Override
    public void start(String id) {
        Optional<BackendContainer> optional = containerRepository.findById(id);
        assert optional.isPresent();
        BackendContainer backendContainer = optional.get();
        backendContainer.setPowerStatus(Container.PowerStatus.ACTIVE);
        client.startContainerCmd(id).exec();
    }

    @Override
    public void update(BackendNetwork entity) {
        networkRepository.saveAndFlush(entity);
    }


}
