package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Null;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record ContainerCreationDTO(
   Integer vcpus,
   Integer ram,
   @JsonProperty("root_disk") Integer rootDisk,
   @JsonProperty("image_id") String imageId,
   @JsonProperty("network_id") String networkId,
   @JsonProperty("custom_name") String customName,
   @Nullable String command,
   @JsonProperty("volumes") @Nullable List<VolumeMountingDTO> volumeMountingDTOs,
   @JsonProperty("env") @Nullable Map<String, String> env
) {
}
