package container.desktop.containerdesktopbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import container.desktop.api.service.VolumeService;
import container.desktop.containerdesktopbackend.DTO.VolumeCreationDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendVolume;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/volumes")
public class VolumeController {

    private final VolumeService<BackendVolume> volumeService;

    public VolumeController(@Qualifier("volume_service") VolumeService<BackendVolume> volumeService) {
        this.volumeService = volumeService;
    }

    @PostMapping("/")
    public ResponseEntity<Result> create(@RequestBody VolumeCreationDTO volumeCreationDTO) {
        String name = volumeService.create(volumeCreationDTO.size());

        Result result = Result.builder()
                .code(HttpStatus.CREATED.value())
                .details(volumeService.findById(name))
                .build();
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
