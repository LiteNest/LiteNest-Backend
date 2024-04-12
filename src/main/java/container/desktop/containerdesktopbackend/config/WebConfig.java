package container.desktop.containerdesktopbackend.config;

import container.desktop.containerdesktopbackend.interceptor.AdminInterceptor;
import container.desktop.containerdesktopbackend.interceptor.ApplicationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApplicationInterceptor applicationInterceptor;
    private final AdminInterceptor adminInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 设置通用拦截器：拦截未登陆的请求，仅放行/login和/register这两个公开接口
        registry.addInterceptor(applicationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/public/**")
                .order(1);
        // 设置管理员用户拦截器：拦截非管理员用户访问管理接口
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .order(2);
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**")
//                .addResourceLocations("classpath:/public/");
//
//    }
}
