package container.desktop.containerdesktopbackend.listener;

import container.desktop.api.entity.User;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.event.VolumeCreatedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class VolumeCreatedEventListener implements ApplicationListener<VolumeCreatedEvent> {

    private final UserService<BackendUser> userService;

    public VolumeCreatedEventListener(UserService<BackendUser> userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(VolumeCreatedEvent event) {
        User user = userService.findById(event.getUserId());
        assert user != null;
        user.addVolumeId(event.getVolume().getId());
        userService.update((BackendUser) user);
    }
}
