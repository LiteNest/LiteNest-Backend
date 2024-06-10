package container.desktop.containerdesktopbackend.controller.admin;

import com.alibaba.fastjson2.JSONObject;
import com.github.dockerjava.api.DockerClient;
import container.desktop.containerdesktopbackend.ContainerDesktopBackendApplication;
import container.desktop.containerdesktopbackend.Result;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@RestController
@RequestMapping("/admin/system")
@AllArgsConstructor
public class AdminSystemMonitorController {

    private final DockerClient client;

    @GetMapping("/")
    public ResponseEntity<Result> getInfo() {
        JSONObject r = new JSONObject();
        r.put("java-version", System.getProperty("java.specification.version"));
        r.put("java-version-date", System.getProperty("java.version.date"));
        r.put("java-runtime-version", System.getProperty("java.runtime.version"));
        r.put("java-vendor-version", System.getProperty("java.vendor.version"));
        r.put("jvm-vendor", System.getProperty("java.vm.vendor"));
        r.put("os-name", System.getProperty("os.name"));
        r.put("os-version", System.getProperty("os.version"));
        r.put("os-arch", System.getProperty("os.arch"));
        r.put("library-path", System.getProperty("sun.boot.library.path"));
        // 获取操作系统相关信息
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int cpuCount = operatingSystemMXBean.getAvailableProcessors(); // 获取可用的CPU核心数
        r.put("cpu-count", cpuCount);
        long totalMemory = Runtime.getRuntime().totalMemory(); // 获取JVM总内存量
        long freeMemory = Runtime.getRuntime().freeMemory();   // 获取JVM空闲内存量
        long maxMemory = Runtime.getRuntime().maxMemory();     // 获取JVM最大可用内存量
        r.put("total-memory", totalMemory);
        r.put("free-memory", freeMemory);
        r.put("max-memory", maxMemory);
        // 获取Docker信息
        r.put("docker-server-version", client.infoCmd().exec().getServerVersion());
        // 获取容器云桌面系统版本
        r.put("version", ContainerDesktopBackendApplication.VERSION);
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();
        r.put("core-number", processor.getLogicalProcessorCount()); // 逻辑处理器数量
        r.put("frequency", processor.getCurrentFreq()); // 各核心频率
        r.put("max-frequency", processor.getMaxFreq());
        return new ResponseEntity<>(Result.ok().setDetails(r), HttpStatus.OK);
    }
}
