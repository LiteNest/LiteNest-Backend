package container.desktop.containerdesktopbackend.interceptor;

import container.desktop.api.entity.User;
import container.desktop.api.repository.UserRepository;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.service.JwtService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ApplicationInterceptor implements Interceptor {

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
        if (header == null || !header.startsWith(TOKEN_HEAD)) {
            unauthorized(response);
            return false;
        }
        String username = verifyJWT(jwtService, userRepository, header.substring(TOKEN_HEAD.length()));
        if (username == null) {
            unauthorized(response);
            return false;
        }
        assert userRepository.findByUsername(username).isPresent();
        onSuccess(request, userRepository.findByUsername(username).get());
        return true;
    }


}
