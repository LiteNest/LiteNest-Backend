package container.desktop.containerdesktopbackend.controller.admin;

import container.desktop.api.entity.User;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService<BackendUser> userService;

    public AdminUserController(UserService<BackendUser> userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<Result> list() {
        List<? extends User> users = userService.list();
        users.forEach(user -> user.setPassword(null));
        return new ResponseEntity<>(Result.ok().setDetails(users), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Result> inspect(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity<>(Result.notFound().setMessage("用户" + userId + "不存在！"), HttpStatus.NOT_FOUND);
        } else {
            user.setPassword(null);
            return new ResponseEntity<>(Result.ok().setDetails(user), HttpStatus.OK);
        }

    }
}
