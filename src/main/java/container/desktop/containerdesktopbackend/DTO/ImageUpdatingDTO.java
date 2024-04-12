package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ImageUpdatingDTO(
        @JsonProperty("available") @Nullable Boolean available,
        @JsonProperty("public") @Nullable Boolean _public,
        @JsonProperty("remote_desktop_port")
        @Nullable
        @Min(1)
        @Max(65535)
        Integer port,
        @JsonProperty("description")
        String description,
        @JsonProperty("min_vcpus")
        Integer minVCPUs,
        @JsonProperty("min_ram")
        Integer minRAM,
        @JsonProperty("min_root_disk")
        Integer minRootDisk
) {
}
