package container.desktop.containerdesktopbackend.interceptor;

import container.desktop.api.entity.User;
import container.desktop.api.repository.UserRepository;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.service.JwtService;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public final class AdminInterceptor implements Interceptor {
    @Value("${jwt.token-header}")
    private String TOKEN_HEADER;
    @Value("${jwt.token-head}")
    private String TOKEN_HEAD;
    private final JwtService jwtService;
    private final UserRepository<BackendUser> userRepository;
    @Override
    public boolean preHandle(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull Object handler)
            throws Exception {

        final String header = request.getHeader(TOKEN_HEADER);
        assert header != null;
        final String token = header.substring(TOKEN_HEAD.length());
        String extractedUsername;
        try {
            extractedUsername = jwtService.extractUsername(token);
        } catch (SignatureException e) {
            unauthorized(response);
            return false;
        }
        Optional<? extends User> optionalUser = userRepository.findByUsername(extractedUsername);
        assert optionalUser.isPresent();
        User user = optionalUser.get();
        if (user.hasRole(User.Role.ADMIN)) {
            onSuccess(request, user);
            return true;
        } else {
            forbidden(response);
            return false;
        }
    }
}
