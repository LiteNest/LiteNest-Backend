package container.desktop.containerdesktopbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContainerDesktopBackendApplication {
    public static long applicationStart = System.nanoTime();
    public static long applicationStarted;
    public static void main(String[] args) {
        System.out.println("Java Runtime Version: " + System.getProperty("java.runtime.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Java Vendor URL: " + System.getProperty("java.vendor.url"));
        System.out.println("Host OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
        SpringApplication.run(ContainerDesktopBackendApplication.class, args);
    }

}
