package container.desktop.containerdesktopbackend.CommandLineRunner;

import container.desktop.api.command.PluginCommand;
import container.desktop.api.locator.PluginAPILocator;
import container.desktop.api.service.ImageService;
import container.desktop.api.service.NetworkService;
import container.desktop.containerdesktopbackend.entity.BackendImage;
import container.desktop.containerdesktopbackend.entity.BackendNetwork;
import container.desktop.containerdesktopbackend.listener.ContextInitializer;
import container.desktop.containerdesktopbackend.service.BackendImageService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

@Component
@Order(10)
public class CommandLineExecutor implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger("命令行执行器");
    @Resource
    private ImageService<BackendImage> imageService;
    @Resource
    private NetworkService<BackendNetwork> networkService;
    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = CommandLineReader.scanner;
        Map<String, PluginCommand> allRegisteredPluginCommandsMap = PluginAPILocator.getPluginManager().getAllRegisteredPluginCommandsMap();
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            String[] commands = line.split("\\s+");
            if (line.isBlank()) continue;
            if (commands.length >= 1){
                if (commands[0].equals("stop")) break;
                if (commands[0].equals("reload")) {
                    imageService.flush();
                    networkService.flush();
                    continue;
                }
                PluginCommand pluginCommand = allRegisteredPluginCommandsMap.get(commands[0]);
                if (pluginCommand != null) {
                    pluginCommand.getCommandExecutor().onCommand(commands[0], Arrays.copyOfRange(commands, 1, commands.length));
                }
                logger.info("未知命令：{}", commands[0]);
            }

        }
        logger.info("正在关闭服务器");
        scanner.close();
        ContextInitializer.getContext().close();

    }
}
