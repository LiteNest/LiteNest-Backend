package container.desktop.containerdesktopbackend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import container.desktop.api.entity.User;
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
@Table(name = "user")
public class BackendUser implements User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    @Enumerated(EnumType.STRING)
    private List<Role> roles;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_containers")
    private List<String> containerIds;

    private transient final Map<String, Object> attributes = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    @CollectionTable(name = "user_metadata")
    private Map<String, String> metadata;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "volume_id")
    @CollectionTable(name = "user_volume_ids")
    @JsonProperty("owned_volume_ids")
    private List<String> volumeIds;



    @Override
    public void addContainer(String containerId) {
        getContainerIds().add(containerId);
    }

    @Override
    public void addRole(Role role) {
        getRoles().add(role);
    }

    @Override
    public void addContainerId(String id) {
        if (getContainerIds() == null) {
            setContainerIds(new ArrayList<>());
        }
        getContainerIds().add(id);
    }

    @Override
    public void removeContainerId(String id) {
        if (getContainerIds() == null) return;
        getContainerIds().remove(id);
    }

    @Override
    public void addVolumeId(String volumeId) {
        if (this.volumeIds == null) {
            this.volumeIds = new LinkedList<>();
        }
        volumeIds.add(volumeId);
    }

    @Override
    public void addVolumeIds(Collection<String> volumeIds) {
        if (this.volumeIds == null) {
            this.volumeIds = new LinkedList<>();
        }
        this.volumeIds.addAll(volumeIds);
    }

    @Override
    public void removeVolumeId(String volumeId) {
        if (this.volumeIds == null) {
            this.volumeIds = new LinkedList<>();
        }
        volumeIds.remove(volumeId);
    }

    @Override
    public void addMetaData(String key, String value) {
        metadata.put(key, value);
    }

    @Override
    public String getMetaData(String key) {
        return metadata.get(key);
    }

    @Override
    public void deleteMetaData(String key) {
        metadata.remove(key);
    }

    @Override
    public void clearMetaData() {
        metadata.clear();
    }

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
