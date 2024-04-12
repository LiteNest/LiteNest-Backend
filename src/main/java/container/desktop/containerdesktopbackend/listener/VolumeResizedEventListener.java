package container.desktop.containerdesktopbackend.listener;

import container.desktop.api.entity.User;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.event.VolumeResizedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class VolumeResizedEventListener implements ApplicationListener<VolumeResizedEvent> {

    private final UserService<BackendUser> userService;

    public VolumeResizedEventListener(@Qualifier("user_service") UserService<BackendUser> userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(VolumeResizedEvent event) {
        User user = userService.findById(event.getUserId());
        assert user != null;
        user.removeVolumeId(event.getOldVolumeId());
        user.addVolumeId(event.getNewVolumeId());
        userService.update((BackendUser) user);
    }
}
