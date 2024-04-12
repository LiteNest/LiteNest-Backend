package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VolumeMountingDTO(
    @JsonProperty("id") String id,
    @JsonProperty("path") String path
) {
}
