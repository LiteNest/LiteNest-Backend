package container.desktop.containerdesktopbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import container.desktop.api.entity.Container;
import container.desktop.api.entity.Network;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "network")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackendNetwork implements Network {
    @Id
    @Column(name = "id")
    private String id;
    private String name;
    private boolean available;
    @Column(name = "is_shown")
    private boolean shown;
    @Column(name = "address")
    @JsonProperty("address")
    private String addr;
    @Column(name = "gateway_address")
    @JsonProperty("gateway_addr")
    private String gatewayAddr;
    @Column(name = "network_driver")
    @Enumerated(EnumType.STRING)
    private NetworkDriver networkDriver;
    @CollectionTable(name = "network_container")
    @ElementCollection(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<String> containerIds;
    private boolean attachable;
    private transient final Map<String, Object> attributes = new HashMap<>();
    private transient List<Container> containers;

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
    public void removeContainerId(String id) {
        if (getContainerIds() == null) {
            setContainerIds(new ArrayList<>());
        }
        getContainerIds().remove(id);
    }

    @Override
    public void addContainerId(String id) {
        if (getContainerIds() == null) {
            return;
        }
        getContainerIds().add(id);
    }
}
