package container.desktop.containerdesktopbackend.controller;

import container.desktop.api.entity.User;
import container.desktop.api.entity.Volume;
import container.desktop.api.exception.IllegalVolumeSizeException;
import container.desktop.api.exception.ResourceNotFoundException;
import container.desktop.api.service.VolumeService;
import container.desktop.containerdesktopbackend.DTO.VolumeCreationDTO;
import container.desktop.containerdesktopbackend.DTO.VolumeUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendVolume;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/volumes")
public class VolumeController {

    private static final Logger log = LoggerFactory.getLogger("卷服务控制器");
    private final VolumeService<BackendVolume> volumeService;

    public VolumeController(@Qualifier("volume_service") VolumeService<BackendVolume> volumeService) {
        this.volumeService = volumeService;
    }

    @PostMapping("/")
    public ResponseEntity<Result> create(@RequestBody VolumeCreationDTO volumeCreationDTO,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        String name = volumeService.create(volumeCreationDTO.size(), volumeCreationDTO.customName(), user.getId());

        Result result = Result.builder()
                .code(HttpStatus.CREATED.value())
                .details(volumeService.findById(name))
                .build();
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{volumeId}")
    public ResponseEntity<Result> delete(@PathVariable String volumeId,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        if (!user.hasVolume(volumeId)) {
            Result result = Result.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("用户" + user.getUsername() + "不持有卷" + volumeId)
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

    @PutMapping("/{volumeId}")
    public ResponseEntity<Result> update(@PathVariable String volumeId,
                                         HttpServletRequest request,
                                         @RequestBody VolumeUpdatingDTO volumeUpdatingDTO){
        User user = (User) request.getAttribute("user");
        if (!user.hasVolume(volumeId)) {
            Result result = Result.builder()
                    .code(404)
                    .message("用户" + user.getUsername() + "不存在卷" + volumeId)
                    .build();
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        } else {
            // todo 变更具体逻辑
            Result result;
            HttpStatus httpStatus;
            try {
                String id = volumeService.resize(volumeId, volumeUpdatingDTO.size(), user.getId());
                result = Result.ok().setDetails(volumeService.findById(id));
                httpStatus = HttpStatus.OK;
            } catch (IllegalVolumeSizeException e) {
                result = Result.builder()
                        .code(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .message(e.getMessage())
                        .build();
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            } catch (ResourceNotFoundException e) {
                result = Result.notFound().setMessage(e.getMessage());
                httpStatus = HttpStatus.NOT_FOUND;
            }
            return new ResponseEntity<>(result, httpStatus);
        }
    }

    @GetMapping("/")
    public ResponseEntity<Result> list(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        log.info("用户{}请求查看其拥有的卷列表", user.getUsername());
        List<? extends Volume> volumes = volumeService.findByIds(user.getVolumeIds());
        Result result = Result.ok().setDetails(volumes);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
