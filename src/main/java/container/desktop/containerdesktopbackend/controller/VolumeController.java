package container.desktop.containerdesktopbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import container.desktop.api.entity.User;
import container.desktop.api.service.VolumeService;
import container.desktop.containerdesktopbackend.DTO.VolumeCreationDTO;
import container.desktop.containerdesktopbackend.DTO.VolumeUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendVolume;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/volumes")
public class VolumeController {

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
            volumeService.resize(volumeId, volumeUpdatingDTO.size(), user.getId());
            return null;
        }
    }
}
