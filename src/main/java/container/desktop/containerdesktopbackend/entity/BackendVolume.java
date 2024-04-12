package container.desktop.containerdesktopbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import container.desktop.api.entity.Volume;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

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
    @JsonProperty("container_ids")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volume_container_id")
    private List<String> containerIds;
    @Column(name = "mount_point")
    @JsonProperty("mount_point")
    private String mountPoint;
    @Column(name = "size")
    private Integer size;
    @Column(name = "owner_id")
    @JsonProperty("owner_id")
    private Long ownerId;

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

    @Override
    public void addContainerId(String id) {
        if (containerIds == null) {
            containerIds = new LinkedList<>();
        }
        containerIds.add(id);
    }

    @Override
    public void removeContainerId(String id) {
        if (containerIds == null) {
            containerIds = new LinkedList<>();
        }
        containerIds.remove(id);
    }

    @Override
    public void addContainerIds(Collection<String> ids) {
        if (containerIds == null) {
            containerIds = new LinkedList<>();
        }
        containerIds.addAll(ids);
    }
}
