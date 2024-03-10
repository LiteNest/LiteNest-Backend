package container.desktop.containerdesktopbackend.CommandLineRunner;

import container.desktop.containerdesktopbackend.ContainerDesktopBackendApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Formatter;

@Component
@Order(2)
public class StartingTimer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger("启动计时器");

    @Override
    public void run(String... args) throws Exception {
        ContainerDesktopBackendApplication.applicationStarted = System.nanoTime();
        double value = (ContainerDesktopBackendApplication.applicationStarted - ContainerDesktopBackendApplication.applicationStart)/1.0e9;
        log.info("启动完成，耗时：{}秒", new Formatter().format("%.2f", value));
    }
}
