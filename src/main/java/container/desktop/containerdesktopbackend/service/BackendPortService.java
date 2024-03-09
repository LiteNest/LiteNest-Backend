package container.desktop.containerdesktopbackend.service;

import container.desktop.api.service.PortService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;

@Service("port_service")
public class BackendPortService implements PortService {
    @Override
    public Integer randomPort() {
        Integer port = null;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            port = serverSocket.getLocalPort();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return port;

    }
}
