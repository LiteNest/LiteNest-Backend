package container.desktop.containerdesktopbackend.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;

public record ContainerUpdatingDTO(
       @JsonProperty("power_status") String powerStatus
) {
}
