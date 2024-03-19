package container.desktop.containerdesktopbackend.listener;

import container.desktop.api.entity.User;
import container.desktop.api.entity.Volume;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.event.VolumeRemovedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class VolumeRemovedEventListener implements ApplicationListener<VolumeRemovedEvent> {

    private final UserService<BackendUser> userService;

    public VolumeRemovedEventListener(UserService<BackendUser> userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(VolumeRemovedEvent event) {
        Volume volume = event.getVolume();
        User user = userService.findById(volume.getOwnerId());
        assert user != null;
        user.removeVolumeId(volume.getId());
        userService.update((BackendUser) user);
    }
}
