package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public record ContainerCreationDTO(
   Integer vcpus,
   Integer ram,
   @JsonProperty("root_disk") Integer rootDisk,
   @JsonProperty("image_id") String imageId,
   @JsonProperty("network_id") String networkId,
   @Nullable String command
) {
}
