package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NetworkUpdatingDTO(
        @JsonProperty("available") Boolean available,
        @JsonProperty("shown") Boolean shown
) {
}
