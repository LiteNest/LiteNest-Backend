package container.desktop.containerdesktopbackend.event;

import container.desktop.api.entity.Volume;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VolumeRemovedEvent extends ApplicationEvent {
    private final Volume volume;
    public VolumeRemovedEvent(Object source, Volume volume) {
        super(source);
        this.volume = volume;
    }

}
