package container.desktop.containerdesktopbackend.event;

import container.desktop.api.entity.Container;
import container.desktop.api.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContainerRemovedEvent extends ApplicationEvent {

    private final Container container;

    public ContainerRemovedEvent(Object source, Container container) {
        super(source);
        this.container = container;
    }
}
