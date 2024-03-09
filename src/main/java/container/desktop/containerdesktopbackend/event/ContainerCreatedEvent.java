package container.desktop.containerdesktopbackend.event;

import container.desktop.api.entity.Container;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class ContainerCreatedEvent extends ApplicationEvent {
    private final Container container;
    public ContainerCreatedEvent(Object source, Container container) {
        super(source);
        this.container = container;
    }
}
