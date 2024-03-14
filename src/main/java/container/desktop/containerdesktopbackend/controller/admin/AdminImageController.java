package container.desktop.containerdesktopbackend.controller.admin;

import container.desktop.api.entity.Image;
import container.desktop.api.exception.ImageUpdatingException;
import container.desktop.api.service.ImageService;
import container.desktop.containerdesktopbackend.DTO.ImageUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendImage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/images")
public class AdminImageController {

    private final ImageService<BackendImage> imageService;

    public AdminImageController(@Qualifier("image_service") ImageService<BackendImage> imageService) {
        this.imageService = imageService;
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<Result> update(@PathVariable String imageId,
                                         @RequestBody @Validated ImageUpdatingDTO updatingDTO,
                                         BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("port")) {
            Result result = Result.builder().code(400).message("无效的端口号").build();
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        Image image = imageService.findById(imageId);
        if (image == null) {
            return new ResponseEntity<>(Result.notFound().setMessage("镜像不存在"), HttpStatus.NOT_FOUND);
        }
        image.setPublic(updatingDTO._public());
        image.setAvailable(updatingDTO.available());
        image.setRemoteDesktopPort(updatingDTO.port());
        image.setDescription(updatingDTO.description());
        image.setMinimumRootDisk(updatingDTO.minRootDisk());
        image.setMinimumRAM(updatingDTO.minRAM());
        image.setMinimumVcpus(updatingDTO.minVCPUs());

        try {
            imageService.update((BackendImage) image);
        } catch (ImageUpdatingException e) {
            HttpStatus status = switch (e.getError()) {
                case DISALLOW_WITHOUT_REMOTE_DESKTOP_PORT -> HttpStatus.UNPROCESSABLE_ENTITY;
            };
            Result result = Result.builder()
                    .code(status.value())
                    .message(e.getError().getMessage())
                    .build();
            return new ResponseEntity<>(result, status);
        }
        return new ResponseEntity<>(Result.ok(), HttpStatus.OK);
    }
}
