package container.desktop.containerdesktopbackend;

import container.desktop.api.command.CommandExecutor;
import container.desktop.api.command.PluginCommand;
import container.desktop.api.plugin.JavaPlugin;
import container.desktop.api.plugin.Plugin;
import container.desktop.api.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class BackendPluginManager implements PluginManager {

    private final Map<String, PluginCommand> pluginCommandMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger("插件管理器");
    Map<String, Plugin> plugins = new HashMap<>();
    private final Set<String> disabledCommandNames = new HashSet<>();
    @Override
    public void loadPlugins(File folder) throws Exception {
        log.info("开始加载插件");
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                Set<String> names = new HashSet<>();
                try(JarFile jarFile = new JarFile(file);
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            String className = entry.getName().replace('/', '.').replace(".class", "");
                            Class<?> clazz = classLoader.loadClass(className);
                            if (JavaPlugin.class.isAssignableFrom(clazz)) {
                                JavaPlugin plugin = (JavaPlugin) clazz.getDeclaredConstructor().newInstance();
                                if (plugins.containsKey(plugin.getName())) {
                                    names.add(plugin.getName());
                                }
                                if (names.contains(plugin.getName())) {
                                    log.warn("检测到重复名称的插件{}，现将跳过加载该插件", plugin.getName());
                                    plugins.remove(plugin.getName());
                                    continue;
                                }
                                plugins.put(plugin.getClass().getName(), plugin);
                                plugin.onEnable();
                            }
                        }
                    }
                }
            }
        }
        log.info("插件加载完成，共加载{}个有效插件", plugins.size());
    }

    @Override
    public void unloadPlugins() {
        plugins.forEach((key, value) -> value.onDisable());
        plugins.clear();
    }

    @Override
    public Plugin[] getPlugins() {
        return plugins.values().toArray(new Plugin[0]);
    }

    @Override
    public Plugin getPlugin(String name) {
        return plugins.values().stream().filter(plugin -> plugin.getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public Map<String, PluginCommand> getAllRegisteredPluginCommandsMap() {
        return pluginCommandMap;
    }

    @Override
    public Collection<PluginCommand> getAllRegisteredPluginCommands() {
        return this.pluginCommandMap.values();
    }

    @Override
    public void registerPluginCommand(Plugin plugin, String name, CommandExecutor commandExecutor) {
        PluginCommand pluginCommand = new PluginCommand(name, plugin);
        if (pluginCommandMap.containsKey(name)) {
            disabledCommandNames.add(name);
        }
        if (disabledCommandNames.contains(name)) {
            log.error("命令“{}”注册失败：与已有插件“{}”提供的命令冲突",name, plugin.getName());
            return;
        }
        this.pluginCommandMap.put(name, pluginCommand);
    }
}
