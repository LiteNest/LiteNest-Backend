package container.desktop.containerdesktopbackend.controller;

import com.alibaba.fastjson2.JSONObject;
import container.desktop.api.entity.User;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.DTO.UserDTO;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService<BackendUser> userService;
    private final JwtService jwtService;

    public UserController(UserService<BackendUser> userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody UserDTO userDTO,
                                        HttpServletRequest request) {
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
            "admin", ((User) request.getAttribute("user")).hasRole(User.Role.ADMIN)));
        }
        Result result = builder.build();
        return new ResponseEntity<>(result, httpStatus);
    }

}
