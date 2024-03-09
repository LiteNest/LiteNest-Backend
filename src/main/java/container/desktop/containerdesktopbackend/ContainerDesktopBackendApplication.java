package container.desktop.containerdesktopbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContainerDesktopBackendApplication {
    public static long applicationStart = System.nanoTime();
    public static long applicationStarted;
    public static void main(String[] args) {
        SpringApplication.run(ContainerDesktopBackendApplication.class, args);
    }

}
