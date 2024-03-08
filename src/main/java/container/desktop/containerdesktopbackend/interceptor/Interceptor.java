package container.desktop.containerdesktopbackend.interceptor;

import com.alibaba.fastjson2.JSONObject;
import container.desktop.api.entity.User;
import container.desktop.api.repository.UserRepository;
import container.desktop.containerdesktopbackend.Result;
import container.desktop.containerdesktopbackend.entity.BackendUser;
import container.desktop.containerdesktopbackend.service.JwtService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public interface Interceptor extends HandlerInterceptor {

    default void unauthorized(@Nonnull HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Result result = Result.builder()
                .code(HttpServletResponse.SC_UNAUTHORIZED)
                .message("Unauthorized Access")
                .build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(JSONObject.toJSONString(result));
    }

    default void forbidden(@Nonnull HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Result result = Result.builder()
                .code(HttpServletResponse.SC_FORBIDDEN)
                .message("Forbidden")
                .build();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(JSONObject.toJSONString(result));
    }

    default void onSuccess(@Nonnull HttpServletRequest request, @Nonnull User user) {
        request.setAttribute("user", user);
    }

    @Nullable
    default String verifyJWT(@Nonnull JwtService service,
                             @Nonnull UserRepository<BackendUser> repository,
                             @Nonnull String token) {
        try {
            String extractedUsername = service.extractUsername(token);
            if (repository.findByUsername(extractedUsername).isPresent()) {
                return extractedUsername;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
