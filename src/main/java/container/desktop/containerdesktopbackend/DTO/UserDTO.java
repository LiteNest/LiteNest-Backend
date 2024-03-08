package container.desktop.containerdesktopbackend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
) {
}
