package container.desktop.containerdesktopbackend.listener;

import container.desktop.api.service.NetworkService;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import container.desktop.containerdesktopbackend.event.ContainerCreatedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ContainerCreatedEventListener implements ApplicationListener<ContainerCreatedEvent> {

    private final NetworkService<BackendNetwork> networkService;

    public ContainerCreatedEventListener(@Qualifier("network_service") NetworkService<BackendNetwork> networkService) {
        this.networkService = networkService;
    }

    @Override
    public void onApplicationEvent(ContainerCreatedEvent event) {
        for (String networkId : event.getContainer().getNetworkIds()) {
            networkService.flush(networkId);
        }
    }
}
