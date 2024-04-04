package container.desktop.containerdesktopbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import container.desktop.api.entity.User;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.DTO.UserDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.service.JwtService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
public class UserController {


    private static final Logger log = LoggerFactory.getLogger("用户控制器");
    private final JwtService jwtService;

    @Resource(name = "user_service")
    private UserService<BackendUser> userService;

    public UserController(JwtService jwtService) {
//        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody UserDTO userDTO) {
        String username = userDTO.username();
        String password = userDTO.password();
        UserService.Status status = userService.login(username, password);
        Result.ResultBuilder builder = Result.builder();
        HttpStatus httpStatus =
            switch (status) {
                case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
                case INVALID_SECRET -> HttpStatus.UNAUTHORIZED;
                default -> HttpStatus.OK;
            };
        builder.code(httpStatus.value());
        if (status == UserService.Status.SUCCESS){
            builder.details(JSONObject.of("token", jwtService.generateToken(username),
            "admin", userService.getByUsername(username).hasRole(User.Role.ADMIN)));
        }
        builder.message(status.getMessage());
        Result result = builder.build();
        return new ResponseEntity<>(result, httpStatus);
    }

    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody UserDTO userDTO) {
        String username = userDTO.username();
        String password = userDTO.password();
        UserService.Status status = userService.register(username, password);
        Result.ResultBuilder builder = Result.builder();
        HttpStatus httpStatus =
                switch (status) {
                    case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
                    case USER_ALREADY_EXISTS -> HttpStatus.CONFLICT;
                    default -> HttpStatus.OK;
                };
        builder.code(httpStatus.value());
        builder.message(status.getMessage());
        Result result = builder.build();
        return new ResponseEntity<>(result, httpStatus);

    }

    @GetMapping("/test")
    public ResponseEntity<Result> test(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        return new ResponseEntity<>(Result.ok().setDetails(Map.of("username", user.getUsername())), HttpStatus.OK);

    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Result> getProfile(@PathVariable Long userId,
                                             HttpServletRequest request){
        User user = (User) request.getAttribute("user");
        if (!Objects.equals(user.getId(), userId)) {
            return new ResponseEntity<>(Result.forbidden().setMessage("无权访问"), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(Result.ok().setDetails(user), HttpStatus.OK);
        }
    }

}
