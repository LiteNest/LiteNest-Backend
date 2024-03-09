package container.desktop.containerdesktopbackend.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import container.desktop.containerdesktopbackend.Unit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ContainerEngineConfig {

    @Value("${container.host}")
    private String host;
    @Value("${container.registry-username}")
    private String registryUsername;
    @Value("${container.registry-password}")
    private String registryPassword;
    @Value("${container.registry-email}")
    private String registryEmail;
    @Value("${container.registry-url}")
    private String registryURL;
    @Value("${container.max-connections}")
    private Integer maxConnections;
    @Value("${container.connection-timeout}")
    private Integer CONNECTION_TIMEOUT;
    @Value("${container.connection-timeout-unit}")
    private Unit CONNECTION_TIMEOUT_UNIT;
    @Value("${container.response-timeout}")
    private Integer RESPONSE_TIMEOUT;
    @Value("${container.response-timeout-unit}")
    private Unit RESPONSE_TIMEOUT_UNIT;


    @Bean
    public DockerClientConfig dockerClientConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .withRegistryUsername(registryUsername)
                .withRegistryPassword(registryPassword)
                .withRegistryEmail(registryEmail)
                .withRegistryUrl(registryURL)
                .withApiVersion("1.44")
                .build();
    }

    @Bean
    public DockerHttpClient dockerHttpClient(DockerClientConfig config) {
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(maxConnections)
                .connectionTimeout(Duration.ofSeconds((long) CONNECTION_TIMEOUT * CONNECTION_TIMEOUT_UNIT.getValue()))
                .responseTimeout(Duration.ofSeconds((long) RESPONSE_TIMEOUT * RESPONSE_TIMEOUT_UNIT.getValue()))
                .build();
    }

    @Bean(name = "docker-client")
    public DockerClient dockerClient(DockerClientConfig config, DockerHttpClient client) {
        return DockerClientImpl.getInstance(config, client);
    }
}
