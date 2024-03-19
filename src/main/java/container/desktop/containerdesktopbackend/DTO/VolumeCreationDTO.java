package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VolumeCreationDTO(
        @JsonProperty("size") Integer size,
        @JsonProperty("custom_name") String customName
) {
}
