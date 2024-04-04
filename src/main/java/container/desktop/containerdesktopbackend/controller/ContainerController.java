package container.desktop.containerdesktopbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import container.desktop.api.entity.Container;
import container.desktop.api.entity.User;
import container.desktop.api.entity.Volume;
import container.desktop.api.exception.ContainerCreationException;
import container.desktop.api.exception.ResourceNotFoundException;
import container.desktop.api.service.ContainerService;
import container.desktop.containerdesktopbackend.DTO.ContainerCreationDTO;
import container.desktop.containerdesktopbackend.DTO.ContainerUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendContainer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/containers")
public class ContainerController {

    private static final Logger log = LoggerFactory.getLogger("容器服务控制器");
    private final ContainerService<BackendContainer> containerService;

    public ContainerController(
            @Qualifier("container_service") ContainerService<BackendContainer> containerService
    ) {
        this.containerService = containerService;
    }

    @GetMapping("/")
    public ResponseEntity<Result> list(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        log.info("用户{}请求查看其拥有的实例列表", user.getUsername());
        List<? extends Container> containers = containerService.findByIds(user.getContainerIds());
        Result result = Result.builder()
                .code(HttpStatus.OK.value())
                .details(containers)
                .build();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Result> create(@RequestBody ContainerCreationDTO containerCreationDTO,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        log.info("用户{}请求创建容器", user.getUsername());
        String id;
        try {
            List<Volume.VolumeBinding> volumeBindings = new ArrayList<>();
            if (containerCreationDTO.volumeMountingDTOs() != null && !containerCreationDTO.volumeMountingDTOs().isEmpty()) {
                volumeBindings.addAll(containerCreationDTO.volumeMountingDTOs().stream().map(
                        volumeMountingDTO -> new Volume.VolumeBinding() {
                            @Override
                            public String getVolumeId() {
                                return volumeMountingDTO.id();
                            }

                            @Override
                            public String getMountPath() {
                                return volumeMountingDTO.path();
                            }
                        }
                ).toList());
            }
            Map<String, String> env = containerCreationDTO.env() == null? Map.of(): containerCreationDTO.env();
            id = containerService.create(containerCreationDTO.customName(),
                    containerCreationDTO.imageId(), containerCreationDTO.networkId(),
                    containerCreationDTO.rootDisk(), containerCreationDTO.vcpus(),
                    containerCreationDTO.ram(), containerCreationDTO.command(),
                    user.getUsername(), env, volumeBindings
            );
        } catch (ContainerCreationException e) {
            Result result = Result.builder()
                    .code(HttpStatus.FORBIDDEN.value())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            Result result = Result.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build();
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }

        log.info("用户{}请求创建的容器{}创建成功", user.getUsername(), id);
        Result result = Result.builder()
                .code(HttpStatus.OK.value())
                .details(containerService.findById(id))
                .build();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{containerId}")
    public ResponseEntity<Result> delete(@PathVariable String containerId,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Container container = containerService.findById(containerId);
        if (!user.hasContainer(containerId) || container == null) {
            return new ResponseEntity<>(Result.builder()
                    .code(404)
                    .message("用户" + user.getUsername() + "不持有容器" + containerId)
                    .build(), HttpStatus.NOT_FOUND);
        }
        containerService.delete(containerId);
        return new ResponseEntity<>(Result.builder()
                .code(200)
                .message("容器" + containerId + "删除成功！")
                .build(), HttpStatus.OK);
    }

    @GetMapping("/{containerId}")
    public ResponseEntity<Result> inspect(@PathVariable String containerId,
                                          HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Container container = containerService.findById(containerId);
        if (!user.hasContainer(containerId) || container == null) {
            return new ResponseEntity<>(Result.builder()
                    .code(404)
                    .message("用户" + user.getUsername() + "不持有容器" + containerId)
                    .build(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(Result.builder()
                .code(200)
                .details(container)
                .build(), HttpStatus.OK);
    }

    @PutMapping("/{containerId}")
    public ResponseEntity<Result> update(@PathVariable String containerId,
                                         @RequestBody ContainerUpdatingDTO updatingDTO,
                                         HttpServletRequest request) {
        String s = updatingDTO.powerStatus();
        Container.PowerStatus status = Container.PowerStatus.parse(s);
        if (status == null) {
            return new ResponseEntity<>(Result.builder().code(400).message("无效的电源状态").build(),
                        HttpStatus.BAD_REQUEST);
        }
        User user = (User) request.getAttribute("user");
        Container container = containerService.findById(containerId);
        if (!user.hasContainer(containerId) || container == null) {
            return new ResponseEntity<>(Result.builder()
                    .code(404)
                    .message("用户" + user.getUsername() + "不持有容器" + containerId)
                    .build(), HttpStatus.NOT_FOUND);
        }
        container.setPowerStatus(status);
        containerService.update((BackendContainer) container);
        Result result = Result.builder()
                .code(200)
                .message("更新容器成功")
                .build();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/maxVCPUs")
    public ResponseEntity<Result> maxVCPUs(){
        Integer maxVCPUs = containerService.getMaxVCPUs();
        return new ResponseEntity<>(Result.ok().setDetails(JSONObject.of("maxVCPUs", maxVCPUs)), HttpStatus.OK);
    }
}
