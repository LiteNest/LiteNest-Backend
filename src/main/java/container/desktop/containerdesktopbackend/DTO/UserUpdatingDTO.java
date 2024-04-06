package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserUpdatingDTO(
        @JsonProperty("password") String password
) {
}
