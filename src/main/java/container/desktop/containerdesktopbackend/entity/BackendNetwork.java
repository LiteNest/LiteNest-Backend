package container.desktop.containerdesktopbackend.entity;

import container.desktop.api.entity.Network;
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
@Table(name = "network")
public class BackendNetwork implements Network {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "address")
    private String addr;
    @Column(name = "gateway_address")
    private String gatewayAddr;
    @Column(name = "network_driver")
    @Enumerated(EnumType.STRING)
    private NetworkDriver networkDriver;
    @CollectionTable(name = "network_container")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> containerIds;
    private boolean attachable;
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
