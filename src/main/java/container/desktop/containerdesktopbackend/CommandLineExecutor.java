package container.desktop.containerdesktopbackend;

import container.desktop.api.command.PluginCommand;
import container.desktop.api.locator.PluginAPILocator;
import container.desktop.containerdesktopbackend.CommandLineRunner.CommandLineReader;
import container.desktop.containerdesktopbackend.listener.ContextInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

@Component
@Order
public class CommandLineExecutor implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger("命令行执行器");
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
