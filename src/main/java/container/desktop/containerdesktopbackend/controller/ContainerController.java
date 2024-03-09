package container.desktop.containerdesktopbackend.controller;

import container.desktop.api.entity.Container;
import container.desktop.api.entity.User;
import container.desktop.api.service.ContainerService;
import container.desktop.containerdesktopbackend.DTO.ContainerCreationDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendContainer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/containers")
public class ContainerController {

    private static final Logger log = LoggerFactory.getLogger(ContainerController.class);
    private final ContainerService<BackendContainer> containerContainerService;

    public ContainerController(
            @Qualifier("container_service") ContainerService<BackendContainer> containerContainerService
    ) {
        this.containerContainerService = containerContainerService;
    }

    @GetMapping("/")
    public ResponseEntity<Result> list(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        log.info("用户{}请求查看其拥有的实例列表", user.getUsername());
        List<? extends Container> containers = containerContainerService.findByIds(user.getContainerIds());
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
        String id = containerContainerService.create(
                containerCreationDTO.imageId(), containerCreationDTO.networkId(),
                containerCreationDTO.rootDisk(), containerCreationDTO.vcpus(),
                containerCreationDTO.ram(), containerCreationDTO.command(),
                user.getUsername()
        );
        log.info("用户{}请求创建的容器{}创建成功", user.getUsername(), id);
        Result result = Result.builder()
                .code(HttpStatus.OK.value())
                .details(containerContainerService.findById(id))
                .build();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
