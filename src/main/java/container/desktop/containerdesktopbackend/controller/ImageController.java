package container.desktop.containerdesktopbackend.controller;

import container.desktop.api.entity.Image;
import container.desktop.api.entity.User;
import container.desktop.api.service.ImageService;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendImage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService<BackendImage> imageService;

    public ImageController(@Qualifier("image_service") ImageService<BackendImage> imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/")
    public ResponseEntity<Result> list(HttpServletRequest request){
        User user = (User) request.getAttribute("user");
        List<? extends Image> images;
        if (user.hasRole(User.Role.ADMIN)) {
            images = imageService.listAll();
        } else {
            images = imageService.listAllPublic();
        }
        return new ResponseEntity<>(Result.builder()
                .code(200)
                .details(images)
                .build(), HttpStatus.OK);
    }

    @GetMapping("/{imageId}")
    private ResponseEntity<Result> update(@PathVariable String imageId,
                                          HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Image image = imageService.findById(imageId);
        if (image == null) {
            Result result = Result.notFound().setMessage("镜像" + imageId + "不存在");
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        if (!image.isPublic() && !user.hasRole(User.Role.ADMIN)) {
            Result result = Result.forbidden().setMessage("访问非公开镜像");
            return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
        }
        Result result = Result.ok().setDetails(image);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
