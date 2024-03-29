package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VolumeUpdatingDTO(
        @JsonProperty("size") Integer size
) {
}
