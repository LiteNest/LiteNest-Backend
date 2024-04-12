package container.desktop.containerdesktopbackend.controller.admin;

import container.desktop.api.entity.Container;
import container.desktop.api.entity.User;
import container.desktop.api.service.ContainerService;
import container.desktop.containerdesktopbackend.DTO.ContainerUpdatingDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendContainer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/containers")
public class AdminContainerController {
    private static final Logger log = LoggerFactory.getLogger("管理员容器服务控制器");
    private final ContainerService<BackendContainer> containerService;

    public AdminContainerController(@Qualifier("container_service") ContainerService<BackendContainer> containerService) {
        this.containerService = containerService;
    }

    @GetMapping("/")
    public ResponseEntity<Result> list(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        log.info("管理员{}请求查看系统内所有容器的列表", user.getUsername());
        List<? extends Container> containers = containerService.list();
        Result result = Result.builder()
                .code(HttpStatus.OK.value())
                .details(containers)
                .build();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{containerId}")
    public ResponseEntity<Result> delete(@PathVariable String containerId,
                                         HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Container container = containerService.findById(containerId);
        if (container == null) {
            return new ResponseEntity<>(Result.builder()
                    .code(404)
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
        if (container == null) {
            return new ResponseEntity<>(Result.builder()
                    .code(404)
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
        if (container == null) {
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

}
