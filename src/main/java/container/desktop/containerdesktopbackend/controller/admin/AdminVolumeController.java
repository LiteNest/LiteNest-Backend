package container.desktop.containerdesktopbackend.controller.admin;

import container.desktop.api.entity.User;
import container.desktop.api.entity.Volume;
import container.desktop.api.exception.IllegalVolumeSizeException;
import container.desktop.api.exception.ResourceNotFoundException;
import container.desktop.api.service.VolumeService;
import container.desktop.containerdesktopbackend.DTO.VolumeUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendVolume;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/admin/volumes")
public class AdminVolumeController {
    private static final Logger log = LoggerFactory.getLogger("管理员卷控制器");
    @Resource(name = "volume_service")
    private VolumeService<BackendVolume> volumeService;
    @PutMapping("/{volumeId}")
    public ResponseEntity<Result> update(@PathVariable String volumeId,
                                         @RequestBody VolumeUpdatingDTO volumeUpdatingDTO){
        Volume volume = volumeService.findById(volumeId);
        if (volume == null) {
            return new ResponseEntity<>(Result.notFound().setMessage("不存在卷" + volumeId), HttpStatus.NOT_FOUND);
        }
        if (volumeUpdatingDTO.customName() != null) {
            volume.setCustomName(volumeUpdatingDTO.customName());
        }
        if (!Objects.equals(volume.getSize(), volumeUpdatingDTO.size())) {
            volume.setSize(volumeUpdatingDTO.size());
            try {
                volumeService.resize(volumeId, volumeUpdatingDTO.size(), volume.getOwnerId());
            } catch (IllegalVolumeSizeException e) {
                return new ResponseEntity<>(Result.builder()
                        .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .message(e.getMessage())
                        .build(), HttpStatus.UNPROCESSABLE_ENTITY);
            } catch (ResourceNotFoundException e) {
                return new ResponseEntity<>(Result.builder()
                        .code(HttpStatus.NOT_FOUND.value())
                        .message("资源不存在")
                        .build(), HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>(Result.ok(), HttpStatus.OK);
    }

    @DeleteMapping("/{volumeId}")
    public ResponseEntity<Result> delete(@PathVariable String volumeId,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (volumeService.findById(volumeId) == null) {
            Result result = Result.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .build();
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        } else {
            volumeService.delete(volumeId);
            Result result = Result.builder()
                    .code(HttpStatus.OK.value())
                    .build();
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }


    @GetMapping("/")
    public ResponseEntity<Result> list(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        log.info("管理员{}请求查看系统中所有的卷列表", user.getUsername());
        List<? extends Volume> volumes = volumeService.list();
        Result result = Result.ok().setDetails(volumes);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
