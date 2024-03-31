package container.desktop.containerdesktopbackend.controller.admin;

import container.desktop.api.entity.Volume;
import container.desktop.api.exception.IllegalVolumeSizeException;
import container.desktop.api.exception.ResourceNotFoundException;
import container.desktop.api.service.VolumeService;
import container.desktop.containerdesktopbackend.DTO.VolumeUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendVolume;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/admin/volumes")
public class AdminVolumeController {
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
}
