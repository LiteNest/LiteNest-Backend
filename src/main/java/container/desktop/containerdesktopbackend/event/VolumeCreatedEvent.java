package container.desktop.containerdesktopbackend.event;

import container.desktop.api.entity.Volume;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VolumeCreatedEvent extends ApplicationEvent {

    private final Long userId;
    private final Volume volume;

    public VolumeCreatedEvent(Object source, Long userId, Volume volume) {
        super(source);
        this.userId = userId;
        this.volume = volume;
    }

}
