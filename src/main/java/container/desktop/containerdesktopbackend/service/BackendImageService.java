package container.desktop.containerdesktopbackend.service;

import com.github.dockerjava.api.DockerClient;
import container.desktop.api.entity.Image;
import container.desktop.api.repository.ImageRepository;
import container.desktop.api.service.ImageService;
import container.desktop.containerdesktopbackend.entity.BackendImage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("image_service")
public class BackendImageService implements ImageService<BackendImage> {

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
    public void flush() {
        List<BackendImage> images = new ArrayList<>();
        client.listImagesCmd().exec().stream().forEach(
                image -> {
                    BackendImage.BackendImageBuilder imageBuilder = BackendImage.builder()
                            .id(image.getId());
                    if (image.getRepoTags().length >= 1){
                        imageBuilder.name(image.getRepoTags()[0]);
                    }
                    BackendImage backendImage = imageBuilder.build();
                    images.add(backendImage);
                }
        );
        imageImageRepository.saveAllAndFlush(images);
    }

    @Override
    public void pull(String id, boolean wait) {

    }

    @Override
    public void update(BackendImage entity) {

    }
}
