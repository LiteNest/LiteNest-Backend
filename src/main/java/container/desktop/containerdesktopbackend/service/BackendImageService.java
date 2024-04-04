package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import container.desktop.api.entity.Image;
import container.desktop.api.exception.ImageUpdatingException;
import container.desktop.api.repository.ImageRepository;
import container.desktop.api.service.ImageService;
import container.desktop.containerdesktopbackend.entity.BackendImage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("image_service")
public class BackendImageService implements ImageService<BackendImage> {

    private final Logger logger = LoggerFactory.getLogger("镜像服务");

    @Value("${container.auto-flush}")
    private boolean autoFlush;

    private final ImageRepository<BackendImage> imageImageRepository;
    private final DockerClient client;

    public BackendImageService(ImageRepository<BackendImage> imageImageRepository, DockerClient client) {
        this.imageImageRepository = imageImageRepository;
        this.client = client;
    }

    @PostConstruct
    public void init(){
        if (autoFlush) flush();
    }

    @Override
    public List<? extends Image> listAll() {
        return imageImageRepository.findAll();
    }

    @Override
    public List<? extends Image> listAllPublic() {
        return imageImageRepository.findAll().stream().filter(BackendImage::isPublic).toList();
    }

    @Override
    public Image findById(String id) {
        return imageImageRepository.findById(id).orElse(null);
    }

    @Override
    public void flush() {
        logger.info("开始刷新镜像数据库");
        long start = System.nanoTime();
        List<BackendImage> images = new ArrayList<>();
        List<String> id = imageImageRepository.findAll().stream().map(BackendImage::getId).toList();
        client.listImagesCmd().exec().forEach(
                image -> {
                    if (!id.contains(image.getId())) {
                        BackendImage.BackendImageBuilder imageBuilder =
                                BackendImage.builder()
                                .id(image.getId());
                        if (image.getRepoTags().length >= 1){
                            imageBuilder.name(image.getRepoTags()[0]);
                        }
                        BackendImage backendImage = imageBuilder.build();
                        images.add(backendImage);
                    }
                }
        );
        imageImageRepository.saveAllAndFlush(images);
        long end = System.nanoTime();
        logger.info("镜像数据库刷新完毕");
        logger.info("镜像数据库刷新用时{}ms", (end-start)/1.0e6);
    }

    @Override
    public void pull(String id, boolean wait) {

    }

    @Override
    public void update(BackendImage entity) throws ImageUpdatingException {
        if (entity.isAvailable() && entity.getRemoteDesktopPort() == null) {
            throw new ImageUpdatingException(ImageUpdatingException.Error.DISALLOW_WITHOUT_REMOTE_DESKTOP_PORT);
        }
        imageImageRepository.saveAndFlush(entity);
    }
}
