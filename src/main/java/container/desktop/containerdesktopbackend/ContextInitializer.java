package container.desktop.containerdesktopbackend;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ContextInitializer implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        
    }
}
