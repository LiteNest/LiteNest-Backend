package container.desktop.containerdesktopbackend.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import container.desktop.api.entity.Volume;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "volume")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackendVolume implements Volume {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "custom_name")
    @JsonProperty("custom_name")
    private String customName;
    @Column(name = "container_id")
    @JsonProperty("container_id")
    private String containerId;
    @Column(name = "mount_point")
    @JsonProperty("mount_point")
    private String mountPoint;
    @Column(name = "size")
    private Integer size;

    private transient Map<String, Object> attributes = new HashMap<>();

    @Override
    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void deleteAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public void clearAttributes() {
        attributes.clear();
    }
}
