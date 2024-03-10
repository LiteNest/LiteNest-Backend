package container.desktop.containerdesktopbackend.CommandLineRunner;

import container.desktop.api.locator.PluginAPILocator;
import container.desktop.api.plugin.PluginManager;
import container.desktop.containerdesktopbackend.BackendPluginManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Order(1)
public class PluginLoader implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        PluginManager pluginManager = new BackendPluginManager();
        File folder = new File("plugins");
        if (!folder.exists()) folder.mkdirs();
        pluginManager.loadPlugins(folder);
        PluginAPILocator.setPluginManager(pluginManager);
    }
}
