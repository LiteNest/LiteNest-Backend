package container.desktop.containerdesktopbackend.listener;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ContextInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Getter(AccessLevel.PUBLIC)
    private static ConfigurableApplicationContext context;

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        context = (ConfigurableApplicationContext) event.getApplicationContext();
    }


}
