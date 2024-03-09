package container.desktop.containerdesktopbackend.listener;

import container.desktop.api.entity.Container;
import container.desktop.api.entity.Network;
import container.desktop.api.entity.User;
import container.desktop.api.service.NetworkService;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.event.ContainerRemovedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContainerRemovedEventListener implements ApplicationListener<ContainerRemovedEvent> {

    private final NetworkService<BackendNetwork> networkService;
    private final UserService<BackendUser> userService;

    public ContainerRemovedEventListener(
            @Qualifier("network_service") NetworkService<BackendNetwork> networkService,
            @Qualifier("user_service") UserService<BackendUser> userService
    ) {
        this.networkService = networkService;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(ContainerRemovedEvent event) {
        Container container = event.getContainer();
        User user = userService.findById(container.getOwnerId());
        assert user != null;
        user.removeContainerId(container.getId());
        userService.update((BackendUser) user);
        List<? extends Network> networks = networkService.findAll().stream()
                .filter(network -> container.getNetworkIds().contains(network.getId())).toList();
        networks.stream().map(network -> (BackendNetwork) network)
                .forEach(backendNetwork -> backendNetwork.removeContainerId(container.getId()));
        networks.forEach(network -> networkService.update((BackendNetwork) network));
    }
}
