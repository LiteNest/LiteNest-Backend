package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record VolumeUpdatingDTO(
        @JsonProperty("size") Integer size,
        @JsonProperty("custom_name") @Nullable String customName
) {
}
