package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import container.desktop.api.entity.Network;
import container.desktop.api.repository.ContainerRepository;
import container.desktop.api.repository.NetworkRepository;
import container.desktop.api.service.NetworkService;
import container.desktop.containerdesktopbackend.entity.BackendContainer;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BackendNetworkService implements NetworkService<BackendNetwork> {

    private static final Logger log = LoggerFactory.getLogger(BackendNetworkService.class);
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
    public void flush() {
        log.info("开始刷新网络数据库");
        long start = System.nanoTime();
        List<BackendNetwork> backendNetworks = new ArrayList<>();
        Set<String> collect = containerRepository.findAll().stream().map(BackendContainer::getId).collect(Collectors.toUnmodifiableSet());
        client.listNetworksCmd().exec()
                .forEach(network -> {
                    BackendNetwork.BackendNetworkBuilder builder
                            = BackendNetwork.builder()
                            .id(network.getId())
                            .name(network.getName())
                            .containerIds(
                                    client.inspectNetworkCmd().withNetworkId(network.getId()).exec().getContainers().keySet()
                                            .stream().parallel().filter(collect::contains).toList()
                            )
                            .networkDriver(Network.NetworkDriver.parse(network.getDriver()));
                    List<com.github.dockerjava.api.model.Network.Ipam.Config> config = network.getIpam().getConfig();
                    if (config != null) {
                        builder.addr(config.getFirst().getSubnet())
                                .gatewayAddr(config.getFirst().getGateway());
                    }
                    BackendNetwork backendNetwork = builder.build();
                    backendNetworks.add(backendNetwork);
                });
        networkRepository.saveAllAndFlush(backendNetworks);
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
    public void update(BackendNetwork entity) {
        networkRepository.saveAndFlush(entity);
    }
}
