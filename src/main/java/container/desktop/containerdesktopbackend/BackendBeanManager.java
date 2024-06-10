package container.desktop.containerdesktopbackend;


import container.desktop.api.BeanManager;
import container.desktop.containerdesktopbackend.listener.ContextInitializer;

public class BackendBeanManager implements BeanManager {
    @Override
    public <T> T getBean(Class<T> requiredType) {
        return ContextInitializer.getContext().getBean(requiredType);
    }

    @Override
    public Object getBean(String name) {
        return ContextInitializer.getContext().getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return ContextInitializer.getContext().getBean(name, requiredType);
    }
}
