package container.desktop.containerdesktopbackend;

import container.desktop.api.entity.User;
import container.desktop.api.repository.UserRepository;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
@Order(1)
public class AdminUserInitializr implements CommandLineRunner {

    private final UserRepository<BackendUser> userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializr(@Qualifier("user_repo") UserRepository<BackendUser> userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findAll().stream().noneMatch(user -> user.hasRole(User.Role.ADMIN))) {
            Scanner scanner = new Scanner(System.in);
            System.out.println();
            System.out.println("系统中没有管理员用户，现在需要创建一个管理员用户！");
            String username, password;
            do {
                System.out.println("请输入用户名：");
                System.out.print("> ");
                username = scanner.nextLine();
            } while (username.isBlank());
            do {
                System.out.println("请输入密码：");
                System.out.print("> ");
                password = scanner.nextLine();
            } while (password.isBlank());
            scanner.close();
            BackendUser backendUser = BackendUser.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .roles(List.of(User.Role.class.getEnumConstants()))
                    .build();
            userRepository.saveAndFlush(backendUser);
            System.out.println("管理员用户初始化完成！");
        }
    }
}
