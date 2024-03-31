package container.desktop.containerdesktopbackend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VolumeResizedEvent extends ApplicationEvent {
    private final Long userId;
    private final String oldVolumeId;
    private final String newVolumeId;

    public VolumeResizedEvent(Object source, Long userId, String oldVolumeId, String newVolumeId) {
        super(source);
        this.userId = userId;
        this.oldVolumeId = oldVolumeId;
        this.newVolumeId = newVolumeId;
    }
}
