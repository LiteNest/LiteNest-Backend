package container.desktop.containerdesktopbackend.controller.admin;

import com.alibaba.fastjson2.JSONObject;
import container.desktop.containerdesktopbackend.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@RestController
@RequestMapping("/admin/system")
public class AdminSystemMonitorController {
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
        return new ResponseEntity<>(Result.ok().setDetails(r), HttpStatus.OK);
    }
}
