package container.desktop.containerdesktopbackend.service;

import container.desktop.api.entity.User;
import container.desktop.api.repository.UserRepository;
import container.desktop.api.service.UserService;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("user_service")
public class BackendUserService implements UserService<BackendUser> {

    private final UserRepository<BackendUser> userRepository;
    private final PasswordEncoder passwordEncoder;

    public BackendUserService(
            @Qualifier("user_repo") UserRepository<BackendUser> userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Status login(String username, String password) {
        Optional<BackendUser> optional = userRepository.findByUsername(username);
        if (optional.isEmpty()) {
            return Status.USER_NOT_FOUND;
        }
        User user = optional.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
            return Status.SUCCESS;
        } else {
            return Status.INVALID_SECRET;
        }
    }

    @Override
    public Status register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return Status.USER_ALREADY_EXISTS;
        }
        userRepository.save(BackendUser.builder()
                        .roles(List.of(User.Role.USER))
                        .username(username)
                        .password(passwordEncoder.encode(password))
                .build());
        return Status.SUCCESS;
    }

    @Override
    public Status changePassword(String username, String password) {
        Optional<BackendUser> optional = userRepository.findByUsername(username);
        if (optional.isEmpty()) {
            return Status.USER_NOT_FOUND;
        }
        BackendUser user = optional.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return Status.SUCCESS;
    }

    @Override
    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public BackendUser getByUsername(String username) {
        Optional<BackendUser> optional = userRepository.findByUsername(username);
        return optional.orElse(null);
    }


    @Nullable
    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<? extends User> list() {
        return userRepository.findAll();
    }

    @Override
    public void update(BackendUser entity) {
        userRepository.saveAndFlush(entity);
    }
}
