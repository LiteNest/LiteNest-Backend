package container.desktop.containerdesktopbackend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import container.desktop.api.entity.Container;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "container")
public class BackendContainer implements Container {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "custom_name")
    @JsonProperty("custom_name")
    private String customName;
    @Column(name = "image_id")
    @JsonProperty("image_id")
    private String imageId;
    @Column(name = "root_disk")
    @JsonProperty("root_disk")
    private Integer rootDisk;
    @Column(name = "vcpus")
    @JsonProperty("vcpus")
    private Integer Vcpus;
    private Integer RAM;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "container_network")
    @JsonProperty("network_ids")
    private List<String> networkIds;
    @Column(name = "power_status")
    @Enumerated(EnumType.STRING)
    @JsonProperty("power_status")
    private PowerStatus powerStatus = PowerStatus.POWER_OFF;
    @Column(name = "owner_id")
    @JsonProperty("owner_id")
    private Long ownerId;
    private transient final Map<String, Object> attributes = new HashMap<>();

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
